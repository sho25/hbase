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
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|Cell
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
name|HBaseConfiguration
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
name|HBaseTestingUtility
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
name|MediumTests
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
name|TableName
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
name|coprocessor
operator|.
name|BaseRegionObserver
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestClientOperationInterrupt
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestClientOperationInterrupt
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|dummy
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dummy"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|test
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|public
specifier|static
class|class
name|TestCoprocessor
extends|extends
name|BaseRegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|2500
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|TestCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
argument_list|)
expr_stmt|;
name|HTable
name|ht
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|dummy
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInterrupt50Percent
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|AtomicInteger
name|noEx
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|badEx
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|noInt
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|done
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
operator|new
name|ArrayList
argument_list|<
name|Thread
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|nbThread
init|=
literal|100
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nbThread
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|HTable
name|ht
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
argument_list|)
decl_stmt|;
name|noEx
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|e
operator|instanceof
name|InterruptedIOException
operator|)
operator|||
operator|(
name|e
operator|instanceof
name|SocketTimeoutException
operator|)
condition|)
block|{
name|badEx
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|isInterrupted
argument_list|()
condition|)
block|{
name|noInt
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"The thread should NOT be with the 'interrupt' status."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|done
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|t
operator|.
name|setName
argument_list|(
literal|"TestClientOperationInterrupt #"
operator|+
name|i
argument_list|)
expr_stmt|;
name|threads
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nbThread
operator|/
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|threads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
name|boolean
name|stillAlive
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|stillAlive
condition|)
block|{
name|stillAlive
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|Thread
name|t
range|:
name|threads
control|)
block|{
if|if
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|stillAlive
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|isInterrupted
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|" noEx: "
operator|+
name|noEx
operator|.
name|get
argument_list|()
operator|+
literal|", badEx="
operator|+
name|badEx
operator|.
name|get
argument_list|()
operator|+
literal|", noInt="
operator|+
name|noInt
operator|.
name|get
argument_list|()
argument_list|,
name|noEx
operator|.
name|get
argument_list|()
operator|==
name|nbThread
operator|/
literal|2
operator|&&
name|badEx
operator|.
name|get
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// The problem here is that we need the server to free its handlers to handle all operations
while|while
condition|(
name|done
operator|.
name|get
argument_list|()
operator|!=
name|nbThread
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|HTable
name|ht
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|r
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

