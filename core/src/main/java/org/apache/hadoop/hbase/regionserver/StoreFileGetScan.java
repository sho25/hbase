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
name|io
operator|.
name|hfile
operator|.
name|HFileScanner
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
name|util
operator|.
name|Bytes
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
comment|/**  * Use to execute a get by scanning all the store files in order.  */
end_comment

begin_class
specifier|public
class|class
name|StoreFileGetScan
block|{
specifier|private
name|List
argument_list|<
name|HFileScanner
argument_list|>
name|scanners
decl_stmt|;
specifier|private
name|QueryMatcher
name|matcher
decl_stmt|;
specifier|private
name|KeyValue
name|startKey
decl_stmt|;
comment|/**    * Constructor    * @param scanners    * @param matcher    */
specifier|public
name|StoreFileGetScan
parameter_list|(
name|List
argument_list|<
name|HFileScanner
argument_list|>
name|scanners
parameter_list|,
name|QueryMatcher
name|matcher
parameter_list|)
block|{
name|this
operator|.
name|scanners
operator|=
name|scanners
expr_stmt|;
name|this
operator|.
name|matcher
operator|=
name|matcher
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|matcher
operator|.
name|getStartKey
argument_list|()
expr_stmt|;
block|}
comment|/**    * Performs a GET operation across multiple StoreFiles.    *<p>    * This style of StoreFile scanning goes through each    * StoreFile in its entirety, most recent first, before    * proceeding to the next StoreFile.    *<p>    * This strategy allows for optimal, stateless (no persisted Scanners)    * early-out scenarios.    * @param result List to add results to    * @throws IOException    */
specifier|public
name|void
name|get
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|HFileScanner
name|scanner
range|:
name|this
operator|.
name|scanners
control|)
block|{
name|this
operator|.
name|matcher
operator|.
name|update
argument_list|()
expr_stmt|;
if|if
condition|(
name|getStoreFile
argument_list|(
name|scanner
argument_list|,
name|result
argument_list|)
operator|||
name|matcher
operator|.
name|isDone
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
block|}
comment|/**    * Performs a GET operation on a single StoreFile.    * @param scanner    * @param result    * @return true if done with this store, false if must continue to next    * @throws IOException    */
specifier|public
name|boolean
name|getStoreFile
parameter_list|(
name|HFileScanner
name|scanner
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scanner
operator|.
name|seekTo
argument_list|(
name|startKey
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|startKey
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|startKey
operator|.
name|getKeyLength
argument_list|()
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
comment|// No keys in StoreFile at or after specified startKey
comment|// First row may be = our row, so we have to check anyways.
name|byte
index|[]
name|firstKey
init|=
name|scanner
operator|.
name|getReader
argument_list|()
operator|.
name|getFirstKey
argument_list|()
decl_stmt|;
comment|// Key may be null if storefile is empty.
if|if
condition|(
name|firstKey
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|short
name|rowLen
init|=
name|Bytes
operator|.
name|toShort
argument_list|(
name|firstKey
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|SIZEOF_SHORT
argument_list|)
decl_stmt|;
name|int
name|rowOffset
init|=
name|Bytes
operator|.
name|SIZEOF_SHORT
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|matcher
operator|.
name|rowComparator
operator|.
name|compareRows
argument_list|(
name|firstKey
argument_list|,
name|rowOffset
argument_list|,
name|rowLen
argument_list|,
name|startKey
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|startKey
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|startKey
operator|.
name|getRowLength
argument_list|()
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
block|}
do|do
block|{
name|KeyValue
name|kv
init|=
name|scanner
operator|.
name|getKeyValue
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|matcher
operator|.
name|match
argument_list|(
name|kv
argument_list|)
condition|)
block|{
case|case
name|INCLUDE
case|:
name|result
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
break|break;
case|case
name|SKIP
case|:
break|break;
case|case
name|NEXT
case|:
return|return
literal|false
return|;
case|case
name|DONE
case|:
return|return
literal|true
return|;
block|}
block|}
do|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
condition|)
do|;
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

