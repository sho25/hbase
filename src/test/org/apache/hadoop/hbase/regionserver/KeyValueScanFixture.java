begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|regionserver
operator|.
name|KeyValueScanner
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|Collections
import|;
end_import

begin_comment
comment|/**  * A fixture that implements and presents a KeyValueScanner.  * It takes a list of key/values which is then sorted according  * to the provided comparator, and then the whole thing pretends  * to be a store file scanner.  */
end_comment

begin_class
specifier|public
class|class
name|KeyValueScanFixture
implements|implements
name|KeyValueScanner
block|{
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
name|data
decl_stmt|;
name|Iterator
argument_list|<
name|KeyValue
argument_list|>
name|iter
init|=
literal|null
decl_stmt|;
name|KeyValue
name|current
init|=
literal|null
decl_stmt|;
name|KeyValue
operator|.
name|KVComparator
name|comparator
decl_stmt|;
specifier|public
name|KeyValueScanFixture
parameter_list|(
name|KeyValue
operator|.
name|KVComparator
name|comparator
parameter_list|,
name|KeyValue
modifier|...
name|incData
parameter_list|)
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|data
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|incData
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|incData
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|data
operator|.
name|add
argument_list|(
name|incData
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|data
argument_list|,
name|this
operator|.
name|comparator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValue
name|peek
parameter_list|()
block|{
return|return
name|this
operator|.
name|current
return|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValue
name|next
parameter_list|()
block|{
name|KeyValue
name|res
init|=
name|current
decl_stmt|;
if|if
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
name|current
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
else|else
name|current
operator|=
literal|null
expr_stmt|;
return|return
name|res
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
block|{
comment|// start at beginning.
name|iter
operator|=
name|data
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|int
name|cmp
decl_stmt|;
name|KeyValue
name|kv
init|=
literal|null
decl_stmt|;
do|do
block|{
if|if
condition|(
operator|!
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
name|kv
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
name|cmp
operator|=
name|comparator
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|kv
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|cmp
operator|>
literal|0
condition|)
do|;
name|current
operator|=
name|kv
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// noop.
block|}
block|}
end_class

end_unit

