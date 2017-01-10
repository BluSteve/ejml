/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.alg.dense.misc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.linsol.ReducedRowEchelonForm_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Generalized checks for Gauss-Jordan implementations
 *
 * @author Peter Abeles
 */
public abstract class GeneralReducedRowEchelonFormChecks_R64 {

    Random rand = new Random(234);

    ReducedRowEchelonForm_F64 alg;

    public GeneralReducedRowEchelonFormChecks_R64(ReducedRowEchelonForm_F64 alg) {
        this.alg = alg;
    }

    /**
     * See if it is reducing systems into RREF
     */
    @Test
    public void testFormat() {
        for( int i = 1; i < 10; i++ ) {
            // test square
            checkFormatRandom(1+i,1+i);
            // test wide
            checkFormatRandom(3 + i, 4 + i * 2);
            // test tall
            checkFormatRandom(4 + i * 2,3 + i);
        }
    }

    /**
     * Solve several linear systems and check against solution
     */
    @Test
    public void testSolution() {
        checkSolutionRandom(3,4,3);
        checkSolutionRandom(3,5,3);
        // Tall won't work because with this test because the system is inconsistent
//        checkSolutionRandom(10,4,3);
    }

    @Test
    public void testSingular() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,4,true,1,2,3,4,3,5,6,7,2,4,6,8,-3,4,9,3);

        DMatrixRow_F64 found = A.copy();
        alg.reduce(found,3);

        checkRref(found,3);

        DMatrixRow_F64 A1 = CommonOps_R64.extract(A,0,3,0,3);
        DMatrixRow_F64 X = CommonOps_R64.extract(found,0,3,3,4);
        DMatrixRow_F64 B = new DMatrixRow_F64(3,1);

        CommonOps_R64.mult(A1,X,B);

        for( int i = 0; i < 3; i++ )
            assertEquals(A.get(i,3),B.get(i,0), UtilEjml.TEST_F64);
    }

    /**
     * Feed it specific matrices and see if it dies a horrible death
     */
    @Test
    public void spotTests() {
        DMatrixRow_F64 A = new DMatrixRow_F64(4,6,true,
                0,0,1,-1,-1,4,
                2,4,2,4,2,4,
                2,4,3,3,3,4,
                3,6,6,3,6,6);

        alg.reduce(A,5);
        checkRref(A,5);
    }

    private void checkFormatRandom(int numRows, int numCols) {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(numRows,numCols,-1,1,rand);
        DMatrixRow_F64 found = A.copy();

        alg.reduce(found,numCols);

        checkRref(found, numCols);
    }

    private void checkSolutionRandom(int numRows, int numCols , int solWidth ) {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(numRows,numCols,-1,1,rand);
        DMatrixRow_F64 found = A.copy();

        alg.reduce(found,solWidth);

        checkRref(found,solWidth);

        DMatrixRow_F64 A1 = CommonOps_R64.extract(A,0,numRows,0,solWidth);
        DMatrixRow_F64 X = CommonOps_R64.extract(found,0,solWidth,solWidth,numCols);
        DMatrixRow_F64 B = new DMatrixRow_F64(numRows,numCols-solWidth);

        CommonOps_R64.mult(A1,X,B);

        for( int i = 0; i < numRows; i++ )
            for( int j = 0; j < numCols-solWidth; j++ )
                assertEquals(A.get(i,j+solWidth),B.get(i,j),UtilEjml.TEST_F64);
    }


    /**
     * Checks to see if the provided matrix is in reduced row echelon format
     * @param A
     */
    private void checkRref(DMatrixRow_F64 A , int systemWidth ) {
        int prevLeading = -1;

        for( int row = 0; row < A.numRows; row++ ) {

            // find the next leading
            for( int col = 0; col < systemWidth; col++ ) {
                double val = A.get(row,col);

                if( val == 1 ) {
                    if( prevLeading > col )
                        fail("The next leading one should be at a later column than the previous");
                    prevLeading = col;

                    for( int i = 0; i < A.numRows; i++ ) {
                        if( i == row ) continue;
                        assertTrue("Column should be all zeros, except at the leading",0==A.get(i,col));
                    }

                    break;
                } else {
                    assertEquals("Should be all zeros before the leading 1", 0, val, UtilEjml.TEST_F64);
                }
            }
        }
    }
}
