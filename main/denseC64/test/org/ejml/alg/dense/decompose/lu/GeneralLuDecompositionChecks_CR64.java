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

package org.ejml.alg.dense.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_C64;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_CR64 {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<DMatrixRow_C64> create(int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(4, 4, -1, 1, rand);
        DMatrixRow_C64 A_orig = A.copy();

        LUDecomposition<DMatrixRow_C64> alg = create(4,4);
        assertTrue(alg.decompose(A));

        boolean modified = !MatrixFeatures_CR64.isEquals(A,A_orig);

        assertEquals(modified, alg.inputModified());
    }

    @Test
    public void testAllReal()
    {
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3, true, 5,0, 2,0, 3,0, 1.5,0, -2,0, 8,0, -3,0, 4.7,0, -0.5,0);

        LUDecomposition<DMatrixRow_C64> alg = create(3,3);
        assertTrue(alg.decompose(A.copy()));

        assertFalse(alg.isSingular());

        DMatrixRow_C64 L = alg.getLower(null);
        DMatrixRow_C64 U = alg.getUpper(null);
        DMatrixRow_C64 P = alg.getPivot(null);

        DMatrixRow_C64 P_tran = new DMatrixRow_C64(P.numCols,P.numRows);
        DMatrixRow_C64 PL = new DMatrixRow_C64(P.numRows,P.numCols);
        DMatrixRow_C64 A_found = new DMatrixRow_C64(A.numRows,A.numCols);

        CommonOps_CR64.transpose(P,P_tran);
        CommonOps_CR64.mult(P_tran, L, PL);
        CommonOps_CR64.mult(PL, U, A_found);

        assertTrue(MatrixFeatures_CR64.isIdentical(A_found,A, UtilEjml.TEST_F64));
    }

    @Test
    public void testDecomposition_square_real()
    {
        for( int i = 2; i <= 20; i++ ) {
            DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(i,i,-1,1,rand);

            for (int j = 1; j < A.getDataLength(); j += 2) {
                A.data[j] = 0;
            }

            checkDecomposition(A);
        }
    }

    @Test
    public void testDecomposition_square_imaginary()
    {
        for( int i = 2; i <= 20; i++ ) {
            DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(i,i,-1,1,rand);

            for (int j = 0; j < A.getDataLength(); j += 2) {
                A.data[j] = 0;
            }

            checkDecomposition(A);
        }
    }

    @Test
    public void testDecomposition_square()
    {
        for( int i = 2; i <= 20; i++ ) {
            DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(i,i,-1,1,rand);

            checkDecomposition(A);
        }
    }

    @Test
    public void testFat() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(2,3,-1,1,rand);

        checkDecomposition(A);
    }

    @Test
    public void testTall() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(3,2,rand);

        checkDecomposition(A);
    }

    @Test
    public void zeroMatrix() {
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3);

        LUDecomposition<DMatrixRow_C64> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        DMatrixRow_C64 L = alg.getLower(null);
        DMatrixRow_C64 U = alg.getUpper(null);

        DMatrixRow_C64 A_found = new DMatrixRow_C64(3,3);
        CommonOps_CR64.mult(L, U, A_found);

        assertFalse(MatrixFeatures_CR64.hasUncountable(A_found));
        assertTrue(MatrixFeatures_CR64.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test
    public void testSingular(){
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6,6, 4,4, 4,4, 0,0);

        LUDecomposition<DMatrixRow_C64> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6.1,6.1, 4,4, 4,4, 0,0);

        LUDecomposition<DMatrixRow_C64> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertFalse(alg.isSingular());
    }

    /**
     * Checks to see how it handles getLower getUpper functions with and without
     * a matrix being provided.
     */
    @Test
    public void getLower_getUpper() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(3,3,rand);

        LUDecomposition<DMatrixRow_C64> alg = create(3,3);

        alg.decompose(A);

        DMatrixRow_C64 L_provided = RandomMatrices_CR64.createRandom(3,3,rand);
        DMatrixRow_C64 U_provided = RandomMatrices_CR64.createRandom(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        DMatrixRow_C64 L_ret = alg.getLower(null);
        DMatrixRow_C64 U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures_CR64.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures_CR64.isEquals(U_provided,U_ret));
    }

    private void checkDecomposition(DMatrixRow_C64 a) {
        LUDecomposition<DMatrixRow_C64> alg = create(a.numRows, a.numCols);
        assertTrue(alg.decompose(a.copy()));

        if( a.numRows <= a.numCols)
            assertFalse(alg.isSingular());

        DMatrixRow_C64 L = alg.getLower(null);
        DMatrixRow_C64 U = alg.getUpper(null);
        DMatrixRow_C64 P = alg.getPivot(null);

        DMatrixRow_C64 P_tran  = new DMatrixRow_C64(P.numCols,P.numRows);
        DMatrixRow_C64 PL      = new DMatrixRow_C64(P_tran.numRows,L.numCols);
        DMatrixRow_C64 A_found = new DMatrixRow_C64(a.numRows, a.numCols);

        CommonOps_CR64.transpose(P, P_tran);
        CommonOps_CR64.mult(P_tran, L, PL);
        CommonOps_CR64.mult(PL, U, A_found);

        assertTrue(MatrixFeatures_CR64.isIdentical(A_found, a, UtilEjml.TEST_F64));
    }
}
