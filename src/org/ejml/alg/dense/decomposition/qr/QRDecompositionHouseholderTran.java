/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Householder QR decomposition is rich in operations along the columns of the matrix.  This can be
 * taken advantage of by solving for the Q matrix in a column major format to reduce the number
 * of CPU cache misses and the number of copies that are performed.
 * </p>
 *
 * @see QRDecompositionHouseholder
 *
 * @author Peter Abeles
 */
// TODO remove QR Col and replace with this one?
// -- On small matrices col seems to be about 10% faster
public class QRDecompositionHouseholderTran implements QRDecomposition {

    /**
     * Where the Q and R matrices are stored.  For speed reasons
     * this is transposed
     */
    protected DenseMatrix64F QR;

    // used internally to store temporary data
    protected double v[];

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'
    protected int minLength;

    // the computed gamma for Q_k matrix
    protected double gammas[];
    // local variables
    protected double gamma;
    protected double tau;

    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize( int numRows , int numCols ) {
        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numCols,numRows);
        int maxLength = Math.max(numCols,numRows);

        if( QR == null ) {
            QR = new DenseMatrix64F(numCols,numRows);
            v = new double[ maxLength ];
            gammas = new double[ minLength ];
        } else {
            QR.reshape(numCols,numRows,false);
        }

        if( v.length < maxLength ) {
            v = new double[ maxLength ];
        }
        if( gammas.length < minLength ) {
            gammas = new double[ minLength ];
        }
    }

    /**
     * Inner matrix that stores the decomposition
     */
    public DenseMatrix64F getQR() {
        return QR;
    }

    /**
     * Computes the Q matrix from the information stored in the QR matrix.  This
     * operation requires about 4(m<sup2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CommonOps.identity(numRows,minLength);
            } else {
                if( Q.numRows != numRows || Q.numCols != minLength ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = CommonOps.identity(numRows);
            } else {
                if( Q.numRows != numRows || Q.numCols != numRows ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        }

        for( int j = minLength-1; j >= 0; j-- ) {
            int diagIndex = j*numRows+j;
            double before = QR.get(diagIndex);
            QR.set(diagIndex , 1 );
            QrHelperFunctions.rank1UpdateMultR(Q,QR.data,j*numRows,gammas[j],j,j,numRows,v);
            QR.set(diagIndex , before );
        }

        return Q;
    }

    /**
     * Returns an upper triangular matrix which is the R in the QR decomposition.
     *
     * @param R An upper triangular matrix.
     * @param compact
     */
    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
        if( R == null ) {
            if( compact ) {
                R = new DenseMatrix64F(minLength,minLength);
            } else
                R = new DenseMatrix64F(numRows,numCols);
        } else {
            if( compact ) {
                if( R.numCols != minLength || R.numRows != minLength )
                    throw new IllegalArgumentException("Unexpected dimensions");
            } else {
                if( R.numCols != numCols || R.numRows != numRows )
                    throw new IllegalArgumentException("Unexpected dimensions");
            }

            for( int i = 0; i < R.numRows; i++ ) {
                int min = Math.min(i,R.numCols);
                for( int j = 0; j < min; j++ ) {
                    R.set(i,j,0);
                }
            }
        }

        for( int i = 0; i < minLength; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                double val = QR.get(j,i);
                R.set(i,j,val);
            }
        }


        return R;
    }

    /**
     * <p>
     * To decompose the matrix 'A' it must have full rank.  'A' is a 'm' by 'n' matrix.
     * It requires about 2n*m<sup>2</sup>-2m<sup>2</sup>/3 flops.
     * </p>
     *
     * <p>
     * The matrix provided here can be of different
     * dimension than the one specified in the constructor.  It just has to be smaller than or equal
     * to it.
     * </p>
     */
    @Override
    public boolean decompose( DenseMatrix64F A ) {
        setExpectedMaxSize(A.numRows, A.numCols);

        CommonOps.transpose(A,QR);

        error = false;

        for( int j = 0; j < minLength; j++ ) {
            householder(j);
            updateA(j);
        }

        return !error;
    }

    @Override
    public boolean modifyInput() {
        return false;
    }

    /**
     * <p>
     * Computes the householder vector "u" for the first column of submatrix j.  Note this is
     * a specialized householder for this problem.  There is some protection against
     * overfloaw and underflow.
     * </p>
     * <p>
     * Q = I - &gamma;uu<sup>T</sup>
     * </p>
     * <p>
     * This function finds the values of 'u' and '&gamma;'.
     * </p>
     *
     * @param j Which submatrix to work off of.
     */
    protected void householder( int j )
    {
        int startQR = j*numRows;
        int endQR = startQR+numRows;
        startQR += j;

        double max = QrHelperFunctions.findMax(QR,startQR,numRows-j);

        if( max == 0.0 ) {
            gamma = 0;
            error = true;
        } else {
            // computes tau and normalizes u by max
            tau = QrHelperFunctions.computeTau(startQR, endQR , QR.data, max);

            // divide u by u_0
            double u_0 = QR.get(startQR) + tau;
            QrHelperFunctions.divideElements(startQR+1,endQR , QR.data, u_0 );

            gamma = u_0/tau;
            tau *= max;
        }

        gammas[j] = gamma;
    }

    /**
     * <p>
     * Takes the results from the householder computation and updates the 'A' matrix.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)A
     * </p>
     *
     * @param w The submatrix.
     */
    protected void updateA( int w )
    {
//        int rowW = w*numRows;
//        int rowJ = rowW + numRows;
//
//        for( int j = w+1; j < numCols; j++ , rowJ += numRows) {
//            double val = QR.get(row) + w];
//
//            for( int k = w+1; k < numRows; k++ ) {
//                val += QR.get(rowW + k)*QR.get(rowJ + k);
//            }
//            val *= gamma;
//
//            QR.minus(rowJ + w,  val );
//            for( int i = w+1; i < numRows; i++ ) {
//                QR.minus(rowJ + i, QR.get(rowW + i)*val);
//            }
//        }
//
//        if( w < numCols ) {
//            QR.set(rowW + w, -tau);
//        }

        int rowW = w*numRows + w + 1;
        int rowJ = rowW + numRows;
        int rowJEnd = rowJ + (numCols-w-1)*numRows;
        int indexWEnd = rowW + numRows - w - 1;

        for( ; rowJEnd != rowJ; rowJ += numRows) {
            double val = QR.get(rowJ - 1);

            int indexW = rowW;
            int indexJ = rowJ;

            while( indexW != indexWEnd ) {
                val += QR.get(indexW++)*QR.get(indexJ++);
            }
            val *= gamma;

            QR.minus(rowJ - 1 , val );
            indexW = rowW;
            indexJ = rowJ;
            while( indexW != indexWEnd ) {
                QR.minus( indexJ++ , QR.get(indexW++)*val);
            }
        }

        if( w < numCols ) {
            QR.set(rowW - 1, -tau );
        }
    }

    public double[] getGammas() {
        return gammas;
    }
}