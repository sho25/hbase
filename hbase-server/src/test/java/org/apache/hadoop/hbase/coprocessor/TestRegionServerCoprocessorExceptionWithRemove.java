begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|RetriesExhaustedWithDetailsException
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
name|Durability
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
name|HRegionServer
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
name|regionserver
operator|.
name|wal
operator|.
name|WALEdit
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Tests unhandled exceptions thrown by coprocessors running on regionserver.  * Expected result is that the master will remove the buggy coprocessor from  * its set of coprocessors and throw a org.apache.hadoop.hbase.exceptions.DoNotRetryIOException  * back to the client.  * (HBASE-4014).  */
end_comment

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
name|TestRegionServerCoprocessorExceptionWithRemove
block|{
specifier|public
specifier|static
class|class
name|BuggyRegionObserver
extends|extends
name|SimpleRegionObserver
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"null"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|Durability
name|durability
parameter_list|)
block|{
name|String
name|tableName
init|=
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTableNameAsString
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
literal|"observed_table"
argument_list|)
condition|)
block|{
name|Integer
name|i
init|=
literal|null
decl_stmt|;
name|i
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// set configure to indicate which cp should be loaded
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|BuggyRegionObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testExceptionFromCoprocessorDuringPut
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Set watches on the zookeeper nodes for all of the regionservers in the
comment|// cluster. When we try to write to TEST_TABLE, the buggy coprocessor will
comment|// cause a NullPointerException, which will cause the regionserver (which
comment|// hosts the region we attempted to write to) to abort. In turn, this will
comment|// cause the nodeDeleted() method of the DeadRegionServer tracker to
comment|// execute, which will set the rsZKNodeDeleted flag to true, which will
comment|// pass this test.
name|byte
index|[]
name|TEST_TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"observed_table"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|table
argument_list|,
name|TEST_FAMILY
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
comment|// Note which regionServer that should survive the buggy coprocessor's
comment|// prePut().
name|HRegionServer
name|regionServer
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// same logic as {@link TestMasterCoprocessorExceptionWithRemove},
comment|// but exception will be RetriesExhaustedWithDetailException rather
comment|// than DoNotRetryIOException. The latter exception is what the RegionServer
comment|// will have actually thrown, but the client will wrap this in a
comment|// RetriesExhaustedWithDetailException.
comment|// We will verify that "DoNotRetryIOException" appears in the text of the
comment|// the exception's detailMessage.
name|boolean
name|threwDNRE
init|=
literal|false
decl_stmt|;
try|try
block|{
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|TEST_FAMILY
argument_list|,
name|ROW
argument_list|,
name|ROW
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedWithDetailsException
name|e
parameter_list|)
block|{
comment|// below, could call instead :
comment|// startsWith("Failed 1 action: DoNotRetryIOException.")
comment|// But that might be too brittle if client-side
comment|// DoNotRetryIOException-handler changes its message.
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"DoNotRetryIOException"
argument_list|)
argument_list|)
expr_stmt|;
name|threwDNRE
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|threwDNRE
argument_list|)
expr_stmt|;
block|}
comment|// Wait 3 seconds for the regionserver to abort: expected result is that
comment|// it will survive and not abort.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|regionServer
operator|.
name|isAborted
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"InterruptedException while waiting for regionserver "
operator|+
literal|"zk node to be deleted."
argument_list|)
expr_stmt|;
block|}
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

