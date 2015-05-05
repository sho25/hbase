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
name|hbase
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
name|CellComparator
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
name|KeyValueUtil
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CompoundBloomFilterBase
implements|implements
name|BloomFilterBase
block|{
comment|/**    * At read time, the total number of chunks. At write time, the number of    * chunks created so far. The first chunk has an ID of 0, and the current    * chunk has the ID of numChunks - 1.    */
specifier|protected
name|int
name|numChunks
decl_stmt|;
comment|/**    * The Bloom filter version. There used to be a DynamicByteBloomFilter which    * had version 2.    */
specifier|public
specifier|static
specifier|final
name|int
name|VERSION
init|=
literal|3
decl_stmt|;
comment|/** Target error rate for configuring the filter and for information */
specifier|protected
name|float
name|errorRate
decl_stmt|;
comment|/** The total number of keys in all chunks */
specifier|protected
name|long
name|totalKeyCount
decl_stmt|;
specifier|protected
name|long
name|totalByteSize
decl_stmt|;
specifier|protected
name|long
name|totalMaxKeys
decl_stmt|;
comment|/** Hash function type to use, as defined in {@link Hash} */
specifier|protected
name|int
name|hashType
decl_stmt|;
comment|/** Comparator used to compare Bloom filter keys */
specifier|protected
name|CellComparator
name|comparator
decl_stmt|;
annotation|@
name|Override
specifier|public
name|long
name|getMaxKeys
parameter_list|()
block|{
return|return
name|totalMaxKeys
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getKeyCount
parameter_list|()
block|{
return|return
name|totalKeyCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getByteSize
parameter_list|()
block|{
return|return
name|totalByteSize
return|;
block|}
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|DUMMY
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
comment|/**    * Prepare an ordered pair of row and qualifier to be compared using    * KeyValue.KeyComparator. This is only used for row-column Bloom    * filters.    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|createBloomKey
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|roffset
parameter_list|,
name|int
name|rlength
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|int
name|qoffset
parameter_list|,
name|int
name|qlength
parameter_list|)
block|{
if|if
condition|(
name|qualifier
operator|==
literal|null
condition|)
name|qualifier
operator|=
name|DUMMY
expr_stmt|;
comment|// Make sure this does not specify a timestamp so that the default maximum
comment|// (most recent) timestamp is used.
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|row
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|,
name|DUMMY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|qualifier
argument_list|,
name|qoffset
argument_list|,
name|qlength
argument_list|)
decl_stmt|;
return|return
name|kv
operator|.
name|getKey
argument_list|()
return|;
block|}
block|}
end_class

end_unit

