begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|client
operator|.
name|Put
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|Reducer
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Emits sorted Puts.  * Reads in all Puts from passed Iterator, sorts them, then emits  * Puts in sorted order.  If lots of columns per row, it will use lots of  * memory sorting.  * @see HFileOutputFormat  * @see KeyValueSortReducer  */
end_comment

begin_class
specifier|public
class|class
name|PutSortReducer
extends|extends
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|void
name|reduce
parameter_list|(
name|ImmutableBytesWritable
name|row
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|Iterable
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|,
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
operator|.
name|Context
name|context
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|InterruptedException
block|{
comment|// although reduce() is called per-row, handle pathological case
name|long
name|threshold
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"putsortreducer.row.threshold"
argument_list|,
literal|2L
operator|*
operator|(
literal|1
operator|<<
literal|30
operator|)
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Put
argument_list|>
name|iter
init|=
name|puts
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
name|map
init|=
operator|new
name|TreeSet
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
name|long
name|curSize
init|=
literal|0
decl_stmt|;
comment|// stop at the end or the RAM threshold
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
operator|&&
name|curSize
operator|<
name|threshold
condition|)
block|{
name|Put
name|p
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
range|:
name|p
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|map
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|curSize
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|context
operator|.
name|setStatus
argument_list|(
literal|"Read "
operator|+
name|map
operator|.
name|size
argument_list|()
operator|+
literal|" entries of "
operator|+
name|map
operator|.
name|getClass
argument_list|()
operator|+
literal|"("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|curSize
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|map
control|)
block|{
name|context
operator|.
name|write
argument_list|(
name|row
argument_list|,
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
name|index
operator|>
literal|0
operator|&&
name|index
operator|%
literal|100
operator|==
literal|0
condition|)
name|context
operator|.
name|setStatus
argument_list|(
literal|"Wrote "
operator|+
name|index
argument_list|)
expr_stmt|;
block|}
comment|// if we have more entries to process
if|if
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
comment|// force flush because we cannot guarantee intra-row sorted order
name|context
operator|.
name|write
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

