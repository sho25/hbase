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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|fail
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
name|SmallTests
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|IsSnapshotDoneRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|IsSnapshotDoneResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|TakeSnapshotRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|TakeSnapshotResponse
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
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_comment
comment|/**  * Test snapshot logic from the client  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSnapshotFromAdmin
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
name|TestSnapshotFromAdmin
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Test that the logic for doing 'correct' back-off based on exponential increase and the max-time    * passed from the server ensures the correct overall waiting for the snapshot to finish.    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testBackoffLogic
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|maxWaitTime
init|=
literal|7500
decl_stmt|;
specifier|final
name|int
name|numRetries
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|pauseTime
init|=
literal|500
decl_stmt|;
comment|// calculate the wait time, if we just do straight backoff (ignoring the expected time from
comment|// master)
name|long
name|ignoreExpectedTime
init|=
literal|0
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
literal|6
condition|;
name|i
operator|++
control|)
block|{
name|ignoreExpectedTime
operator|+=
name|HConstants
operator|.
name|RETRY_BACKOFF
index|[
name|i
index|]
operator|*
name|pauseTime
expr_stmt|;
block|}
comment|// the correct wait time, capping at the maxTime/tries + fudge room
specifier|final
name|long
name|time
init|=
name|pauseTime
operator|*
literal|3
operator|+
operator|(
operator|(
name|maxWaitTime
operator|/
name|numRetries
operator|)
operator|*
literal|3
operator|)
operator|+
literal|300
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Capped snapshot wait time isn't less that the uncapped backoff time "
operator|+
literal|"- further testing won't prove anything."
argument_list|,
name|time
operator|<
name|ignoreExpectedTime
argument_list|)
expr_stmt|;
comment|// setup the mocks
name|HConnectionManager
operator|.
name|HConnectionImplementation
name|mockConnection
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HConnectionManager
operator|.
name|HConnectionImplementation
operator|.
name|class
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// setup the conf to match the expected properties
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
name|numRetries
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
name|pauseTime
argument_list|)
expr_stmt|;
comment|// mock the master admin to our mock
name|MasterAdminKeepAliveConnection
name|mockMaster
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterAdminKeepAliveConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockConnection
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockConnection
operator|.
name|getKeepAliveMasterAdmin
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockMaster
argument_list|)
expr_stmt|;
comment|// set the max wait time for the snapshot to complete
name|TakeSnapshotResponse
name|response
init|=
name|TakeSnapshotResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setExpectedTimeout
argument_list|(
name|maxWaitTime
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockMaster
operator|.
name|snapshot
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|isNull
argument_list|()
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|TakeSnapshotRequest
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|response
argument_list|)
expr_stmt|;
comment|// setup the response
name|IsSnapshotDoneResponse
operator|.
name|Builder
name|builder
init|=
name|IsSnapshotDoneResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setDone
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// first five times, we return false, last we get success
name|Mockito
operator|.
name|when
argument_list|(
name|mockMaster
operator|.
name|isSnapshotDone
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|isNull
argument_list|()
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|IsSnapshotDoneRequest
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|builder
operator|.
name|setDone
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// setup the admin and run the test
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|mockConnection
argument_list|)
decl_stmt|;
name|String
name|snapshot
init|=
literal|"snapshot"
decl_stmt|;
name|String
name|table
init|=
literal|"table"
decl_stmt|;
comment|// get start time
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshot
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|long
name|finish
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|elapsed
init|=
operator|(
name|finish
operator|-
name|start
operator|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Elapsed time:"
operator|+
name|elapsed
operator|+
literal|" is more than expected max:"
operator|+
name|time
argument_list|,
name|elapsed
operator|<=
name|time
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Make sure that we validate the snapshot name and the table name before we pass anything across    * the wire    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testValidateSnapshotName
parameter_list|()
throws|throws
name|Exception
block|{
name|HConnectionManager
operator|.
name|HConnectionImplementation
name|mockConnection
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HConnectionManager
operator|.
name|HConnectionImplementation
operator|.
name|class
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockConnection
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|mockConnection
argument_list|)
decl_stmt|;
name|SnapshotDescription
operator|.
name|Builder
name|builder
init|=
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|// check that invalid snapshot names fail
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"-snapshot"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"snapshot fails"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"snap$hot"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// check the table name also get verified
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|".table"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|"-table"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|"table fails"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|failSnapshotStart
argument_list|(
name|admin
argument_list|,
name|builder
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|"tab%le"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// mock the master connection
name|MasterAdminKeepAliveConnection
name|master
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterAdminKeepAliveConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockConnection
operator|.
name|getKeepAliveMasterAdmin
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|TakeSnapshotResponse
name|response
init|=
name|TakeSnapshotResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setExpectedTimeout
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|master
operator|.
name|snapshot
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|isNull
argument_list|()
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|TakeSnapshotRequest
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|IsSnapshotDoneResponse
name|doneResponse
init|=
name|IsSnapshotDoneResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setDone
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|master
operator|.
name|isSnapshotDone
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|isNull
argument_list|()
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|IsSnapshotDoneRequest
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|doneResponse
argument_list|)
expr_stmt|;
comment|// make sure that we can use valid names
name|admin
operator|.
name|snapshot
argument_list|(
name|builder
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|"table"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|failSnapshotStart
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Snapshot should not have succeed with name:"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Correctly failed to start snapshot:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

