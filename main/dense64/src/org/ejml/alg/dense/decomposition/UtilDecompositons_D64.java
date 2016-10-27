/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * @author Peter Abeles
 */
public class UtilDecompositons_D64 {
    public static DenseMatrix64F checkIdentity( DenseMatrix64F Q , int M, int N) {
        if( Q == null ) {
            return CommonOps.identity(M,N);
        } else if( M != Q.numRows || N != Q.numCols )
            throw new IllegalArgumentException("Input is not "+M+" x "+N+" matrix");
        else
            CommonOps.setIdentity(Q);
        return Q;
    }

    public static DenseMatrix64F checkZeros( DenseMatrix64F Q , int M , int N) {
        if( Q == null ) {
            return new DenseMatrix64F(M,N);
        } else if( M != Q.numRows || N != Q.numCols )
            throw new IllegalArgumentException("Input is not "+M+" x "+N+" matrix");
        else
            Q.zero();
        return Q;
    }

}
