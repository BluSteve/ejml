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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_C64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_CR64 {
    
    Random rand = new Random(234);

    @Test
    public void createRandom_min_max() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(30,20,-1,1,rand);

        checkRandomRange(A);
    }

    @Test
    public void setRandom() {
        DMatrixRow_C64 A = new DMatrixRow_C64(5,4);

        RandomMatrices_CR64.setRandom(A,rand);

        checkRandom1(A);
    }

    private void checkRandom1(DMatrixRow_C64 a) {
        assertEquals(5, a.numRows);
        assertEquals(4, a.numCols);

        double totalReal = 0;
        double totalImg = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double real = a.getReal(i,j);
                double img = a.getImag(i, j);

                assertTrue( real >= 0);
                assertTrue( real <= 1);
                totalReal += real;

                assertTrue( img >= 0);
                assertTrue( img <= 1);
                totalImg += img;
            }
        }

        assertTrue(totalReal>0);
        assertTrue(totalImg>0);
    }

    @Test
    public void setRandom_min_max() {
        DMatrixRow_C64 A = new DMatrixRow_C64(30,20);
        RandomMatrices_CR64.setRandom(A,-1,1,rand);

        checkRandomRange(A);
    }

    private void checkRandomRange(DMatrixRow_C64 a) {
        assertEquals(30, a.numRows);
        assertEquals(20, a.numCols);

        int numRealNeg = 0;
        int numRealPos = 0;
        int numImgNeg = 0;
        int numImgPos = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double real = a.getReal(i,j);
                double img = a.getImag(i,j);

                if( real < 0 )
                    numRealNeg++;
                else
                    numRealPos++;

                if( Math.abs(real) > 1 )
                    fail("Out of range");

                if( img < 0 )
                    numImgNeg++;
                else
                    numImgPos++;

                if( Math.abs(img) > 1 )
                    fail("Out of range");
            }
        }

        assertTrue(numRealNeg>0);
        assertTrue(numRealPos>0);

        assertTrue(numImgNeg>0);
        assertTrue(numImgPos>0);
    }


    @Test
    public void createHermPosDef() {
        for( int i = 1; i < 20; i++ ) {
            DMatrixRow_C64 A = RandomMatrices_CR64.createHermPosDef(i, rand);

            assertTrue(MatrixFeatures_CR64.isPositiveDefinite(A));
        }
    }

    @Test
    public void createHermitian() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createHermitian(10, -1, 1, rand);

        assertTrue(MatrixFeatures_CR64.isHermitian(A, UtilEjml.TEST_F64));

        // see if it has the expected range of elements
        double min = CommonOps_CR64.elementMinReal(A);
        double max = CommonOps_CR64.elementMaxReal(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <=  1);

        min = CommonOps_CR64.elementMinImaginary(A);
        max = CommonOps_CR64.elementMaxImaginary(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <=  1);
    }
//
//    @Test
//    public void createUpperTriangle() {
//        for( int hess = 0; hess < 3; hess++ ) {
//            DMatrixRow_C64 A = RandomMatrices_CR64.createUpperTriangle(10,hess,-1,1,rand);
//
//            assertTrue(MatrixFeatures.isUpperTriangle(A,hess,UtilEjml.TEST_F64));
//
//            // quick sanity check to make sure it could be proper
//            assertTrue(A.get(hess,0) != 0 );
//
//            // see if it has the expected range of elements
//            double min = CommonOps.elementMin(A);
//            double max = CommonOps.elementMax(A);
//
//            assertTrue(min < 0 && min >= -1);
//            assertTrue(max > 0 && max <= 1);
//        }
//    }
}
