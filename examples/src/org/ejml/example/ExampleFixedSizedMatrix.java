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

package org.ejml.example;

import org.ejml.alg.fixed.FixedOps3_F64;
import org.ejml.data.DMatrixFixed3_F64;
import org.ejml.data.DMatrixFixed3x3_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.ConvertMatrixStruct_F64;
import org.ejml.simple.SimpleMatrix;

/**
 * In some applications a small fixed sized matrix can speed things up a lot, e.g. 8 times faster.  One application
 * which uses small matrices is graphics and rigid body motion, which extensively uses 3x3 and 4x4 matrices.  This
 * example is to show some examples of how you can use a fixed sized matrix.
 *
 * @author Peter Abeles
 */
public class ExampleFixedSizedMatrix {

    public static void main( String args[] ) {
        // declare the matrix
        DMatrixFixed3x3_F64 a = new DMatrixFixed3x3_F64();
        DMatrixFixed3x3_F64 b = new DMatrixFixed3x3_F64();

        // Can assign values the usual way
        for( int i = 0; i < 3; i++ ) {
            for( int j = 0; j < 3; j++ ) {
                a.set(i,j,i+j+1);
            }
        }

        // Direct manipulation of each value is the fastest way to assign/read values
        a.a11 = 12;
        a.a23 = 64;

        // can print the usual way too
        a.print();

        // most of the standard operations are support
        FixedOps3_F64.transpose(a,b);
        b.print();

        System.out.println("Determinant = "+ FixedOps3_F64.det(a));

        // matrix-vector operations are also supported
        // Constructors for vectors and matrices can be used to initialize its value
        DMatrixFixed3_F64 v = new DMatrixFixed3_F64(1,2,3);
        DMatrixFixed3_F64 result = new DMatrixFixed3_F64();

        FixedOps3_F64.mult(a,v,result);

        // Conversion into DMatrixRow_F64 can also be done
        DMatrixRow_F64 dm = ConvertMatrixStruct_F64.convert(a,null);

        dm.print();

        // This can be useful if you need do more advanced operations
        SimpleMatrix sv = SimpleMatrix.wrap(dm).svd().getV();

        // can then convert it back into a fixed matrix
        DMatrixFixed3x3_F64 fv = ConvertMatrixStruct_F64.convert(sv.matrix_F64(),(DMatrixFixed3x3_F64)null);

        System.out.println("Original simple matrix and converted fixed matrix");
        sv.print();
        fv.print();
    }
}
