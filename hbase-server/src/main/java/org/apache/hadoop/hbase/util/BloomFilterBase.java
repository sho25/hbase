begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
operator|.
name|KVComparator
import|;
end_import

begin_comment
comment|/**  * Common methods Bloom filter methods required at read and write time.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|BloomFilterBase
block|{
comment|/**    * @return The number of keys added to the bloom    */
name|long
name|getKeyCount
parameter_list|()
function_decl|;
comment|/**    * @return The max number of keys that can be inserted    *         to maintain the desired error rate    */
name|long
name|getMaxKeys
parameter_list|()
function_decl|;
comment|/**    * @return Size of the bloom, in bytes    */
name|long
name|getByteSize
parameter_list|()
function_decl|;
comment|/**    * Create a key for a row-column Bloom filter.    */
name|byte
index|[]
name|createBloomKey
parameter_list|(
name|byte
index|[]
name|rowBuf
parameter_list|,
name|int
name|rowOffset
parameter_list|,
name|int
name|rowLen
parameter_list|,
name|byte
index|[]
name|qualBuf
parameter_list|,
name|int
name|qualOffset
parameter_list|,
name|int
name|qualLen
parameter_list|)
function_decl|;
comment|/**    * @return Bloom key comparator    */
name|KVComparator
name|getComparator
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

