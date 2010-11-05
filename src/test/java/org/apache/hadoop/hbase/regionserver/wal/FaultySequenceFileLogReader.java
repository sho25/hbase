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
operator|.
name|wal
package|;
end_package

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
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
operator|.
name|Entry
import|;
end_import

begin_class
specifier|public
class|class
name|FaultySequenceFileLogReader
extends|extends
name|SequenceFileLogReader
block|{
enum|enum
name|FailureType
block|{
name|BEGINNING
block|,
name|MIDDLE
block|,
name|END
block|,
name|NONE
block|}
name|Queue
argument_list|<
name|Entry
argument_list|>
name|nextQueue
init|=
operator|new
name|LinkedList
argument_list|<
name|Entry
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numberOfFileEntries
init|=
literal|0
decl_stmt|;
name|FailureType
name|getFailureType
parameter_list|()
block|{
return|return
name|FailureType
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"faultysequencefilelogreader.failuretype"
argument_list|,
literal|"NONE"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HLog
operator|.
name|Entry
name|next
parameter_list|(
name|HLog
operator|.
name|Entry
name|reuse
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|entryStart
operator|=
name|this
operator|.
name|reader
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|boolean
name|b
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|nextQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Read the whole thing at once and fake reading
while|while
condition|(
name|b
operator|==
literal|true
condition|)
block|{
name|HLogKey
name|key
init|=
name|HLog
operator|.
name|newKey
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|WALEdit
name|val
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|HLog
operator|.
name|Entry
name|e
init|=
operator|new
name|HLog
operator|.
name|Entry
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
decl_stmt|;
name|b
operator|=
name|this
operator|.
name|reader
operator|.
name|next
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getEdit
argument_list|()
argument_list|)
expr_stmt|;
name|nextQueue
operator|.
name|offer
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|numberOfFileEntries
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nextQueue
operator|.
name|size
argument_list|()
operator|==
name|this
operator|.
name|numberOfFileEntries
operator|&&
name|getFailureType
argument_list|()
operator|==
name|FailureType
operator|.
name|BEGINNING
condition|)
block|{
throw|throw
name|this
operator|.
name|addFileInfoToException
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"fake Exception"
argument_list|)
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|nextQueue
operator|.
name|size
argument_list|()
operator|==
name|this
operator|.
name|numberOfFileEntries
operator|/
literal|2
operator|&&
name|getFailureType
argument_list|()
operator|==
name|FailureType
operator|.
name|MIDDLE
condition|)
block|{
throw|throw
name|this
operator|.
name|addFileInfoToException
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"fake Exception"
argument_list|)
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|nextQueue
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|getFailureType
argument_list|()
operator|==
name|FailureType
operator|.
name|END
condition|)
block|{
throw|throw
name|this
operator|.
name|addFileInfoToException
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"fake Exception"
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|nextQueue
operator|.
name|peek
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|edit
operator|++
expr_stmt|;
block|}
name|Entry
name|e
init|=
name|nextQueue
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|e
operator|.
name|getEdit
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|e
return|;
block|}
block|}
end_class

end_unit

