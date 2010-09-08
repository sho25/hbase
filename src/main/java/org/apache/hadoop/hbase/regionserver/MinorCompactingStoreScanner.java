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
name|HConstants
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
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  * A scanner that does a minor compaction at the same time.  Doesn't need to  * implement ChangedReadersObserver, since it doesn't scan memstore, only store files  * and optionally the memstore-snapshot.  */
end_comment

begin_class
specifier|public
class|class
name|MinorCompactingStoreScanner
implements|implements
name|KeyValueScanner
implements|,
name|InternalScanner
block|{
specifier|private
name|KeyValueHeap
name|heap
decl_stmt|;
specifier|private
name|KeyValue
operator|.
name|KVComparator
name|comparator
decl_stmt|;
name|MinorCompactingStoreScanner
parameter_list|(
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|)
throws|throws
name|IOException
block|{
name|comparator
operator|=
name|store
operator|.
name|comparator
expr_stmt|;
name|KeyValue
name|firstKv
init|=
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|firstKv
argument_list|)
expr_stmt|;
block|}
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|store
operator|.
name|comparator
argument_list|)
expr_stmt|;
block|}
name|MinorCompactingStoreScanner
parameter_list|(
name|String
name|cfName
parameter_list|,
name|KeyValue
operator|.
name|KVComparator
name|comparator
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
name|KeyValue
name|firstKv
init|=
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValueScanner
name|scanner
range|:
name|scanners
control|)
block|{
name|scanner
operator|.
name|seek
argument_list|(
name|firstKv
argument_list|)
expr_stmt|;
block|}
name|heap
operator|=
operator|new
name|KeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
block|}
specifier|public
name|KeyValue
name|peek
parameter_list|()
block|{
return|return
name|heap
operator|.
name|peek
argument_list|()
return|;
block|}
specifier|public
name|KeyValue
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|heap
operator|.
name|next
argument_list|()
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
comment|// cant seek.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't seek a MinorCompactingStoreScanner"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|reseek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
block|{
return|return
name|seek
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**    * High performance merge scan.    * @param writer    * @return True if more.    * @throws IOException    */
specifier|public
name|boolean
name|next
parameter_list|(
name|StoreFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|KeyValue
name|row
init|=
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|KeyValue
name|kv
decl_stmt|;
while|while
condition|(
operator|(
name|kv
operator|=
name|heap
operator|.
name|peek
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// check to see if this is a different row
if|if
condition|(
name|comparator
operator|.
name|compareRows
argument_list|(
name|row
argument_list|,
name|kv
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|// reached next row
return|return
literal|true
return|;
block|}
name|writer
operator|.
name|append
argument_list|(
name|heap
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
name|KeyValue
name|row
init|=
name|heap
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|KeyValue
name|kv
decl_stmt|;
while|while
condition|(
operator|(
name|kv
operator|=
name|heap
operator|.
name|peek
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// check to see if this is a different row
if|if
condition|(
name|comparator
operator|.
name|compareRows
argument_list|(
name|row
argument_list|,
name|kv
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|// reached next row
return|return
literal|true
return|;
block|}
name|results
operator|.
name|add
argument_list|(
name|heap
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
parameter_list|,
name|int
name|limit
parameter_list|)
throws|throws
name|IOException
block|{
comment|// should not use limits with minor compacting store scanner
return|return
name|next
argument_list|(
name|results
argument_list|)
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
name|heap
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceID
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

