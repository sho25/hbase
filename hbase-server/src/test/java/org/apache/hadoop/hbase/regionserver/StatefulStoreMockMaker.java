begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
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
name|compactions
operator|.
name|CompactionContext
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
name|compactions
operator|.
name|CompactionRequest
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
name|security
operator|.
name|User
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_comment
comment|/**  * This class is a helper that allows to create a partially-implemented, stateful mocks of  * Store. It contains a bunch of blank methods, and answers redirecting to these.  */
end_comment

begin_class
specifier|public
class|class
name|StatefulStoreMockMaker
block|{
comment|// Add and expand the methods and answers as needed.
specifier|public
name|CompactionContext
name|selectCompaction
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|cancelCompaction
parameter_list|(
name|Object
name|originalContext
parameter_list|)
block|{}
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
specifier|private
class|class
name|SelectAnswer
implements|implements
name|Answer
argument_list|<
name|CompactionContext
argument_list|>
block|{
specifier|public
name|CompactionContext
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|selectCompaction
argument_list|()
return|;
block|}
block|}
specifier|private
class|class
name|PriorityAnswer
implements|implements
name|Answer
argument_list|<
name|Integer
argument_list|>
block|{
specifier|public
name|Integer
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|getPriority
argument_list|()
return|;
block|}
block|}
specifier|private
class|class
name|CancelAnswer
implements|implements
name|Answer
argument_list|<
name|Object
argument_list|>
block|{
specifier|public
name|CompactionContext
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|cancelCompaction
argument_list|(
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|Store
name|createStoreMock
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|Exception
block|{
name|Store
name|store
init|=
name|mock
argument_list|(
name|Store
operator|.
name|class
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|store
operator|.
name|requestCompaction
argument_list|(
name|anyInt
argument_list|()
argument_list|,
name|isNull
argument_list|(
name|CompactionRequest
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|then
argument_list|(
operator|new
name|SelectAnswer
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|requestCompaction
argument_list|(
name|anyInt
argument_list|()
argument_list|,
name|isNull
argument_list|(
name|CompactionRequest
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|User
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|then
argument_list|(
operator|new
name|SelectAnswer
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getCompactPriority
argument_list|()
argument_list|)
operator|.
name|then
argument_list|(
operator|new
name|PriorityAnswer
argument_list|()
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
operator|new
name|CancelAnswer
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|store
argument_list|)
operator|.
name|cancelRequestedCompaction
argument_list|(
name|any
argument_list|(
name|CompactionContext
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|store
return|;
block|}
block|}
end_class

end_unit

