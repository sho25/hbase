begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRawAsyncTableScan
extends|extends
name|AbstractTestAsyncTableScan
block|{
annotation|@
name|Parameter
argument_list|(
literal|0
argument_list|)
specifier|public
name|String
name|scanType
decl_stmt|;
annotation|@
name|Parameter
argument_list|(
literal|1
argument_list|)
specifier|public
name|Supplier
argument_list|<
name|Scan
argument_list|>
name|scanCreater
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: type={0}"
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|getScanCreater
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|p
lambda|->
operator|new
name|Object
index|[]
block|{
name|p
operator|.
name|getFirst
argument_list|()
block|,
name|p
operator|.
name|getSecond
argument_list|()
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Scan
name|createScan
parameter_list|()
block|{
return|return
name|scanCreater
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Result
argument_list|>
name|doScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
block|{
name|SimpleRawScanResultConsumer
name|scanConsumer
init|=
operator|new
name|SimpleRawScanResultConsumer
argument_list|()
decl_stmt|;
name|ASYNC_CONN
operator|.
name|getRawTable
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|scan
argument_list|(
name|scan
argument_list|,
name|scanConsumer
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|result
init|;
operator|(
name|result
operator|=
name|scanConsumer
operator|.
name|take
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scan
operator|.
name|getBatch
argument_list|()
operator|>
literal|0
condition|)
block|{
name|results
operator|=
name|convertFromBatchResult
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
block|}
end_class

end_unit

