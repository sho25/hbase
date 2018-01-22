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
name|replication
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
name|client
operator|.
name|Get
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
name|Result
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
name|LargeTests
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
name|ReplicationTests
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|assertArrayEquals
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationDisableInactivePeer
extends|extends
name|TestReplicationBase
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestReplicationDisableInactivePeer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Test disabling an inactive peer. Add a peer which is inactive, trying to    * insert, disable the peer, then activate the peer and make sure nothing is    * replicated. In Addition, enable the peer and check the updates are    * replicated.    *    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|600000
argument_list|)
specifier|public
name|void
name|testDisableInactivePeer
parameter_list|()
throws|throws
name|Exception
block|{
name|utility2
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|byte
index|[]
name|rowkey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"disable inactive peer"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowkey
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|row
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// wait for the sleep interval of the master cluster to become long
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
operator|*
name|NB_RETRIES
argument_list|)
expr_stmt|;
comment|// disable and start the peer
name|admin
operator|.
name|disablePeer
argument_list|(
literal|"2"
argument_list|)
expr_stmt|;
name|utility2
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rowkey
argument_list|)
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
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|res
init|=
name|htable2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|size
argument_list|()
operator|>=
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Replication wasn't disabled"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not replicated, let's wait a bit more..."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Test enable replication
name|admin
operator|.
name|enablePeer
argument_list|(
literal|"2"
argument_list|)
expr_stmt|;
comment|// wait since the sleep interval would be long
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
operator|*
name|NB_RETRIES
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
name|NB_RETRIES
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|res
init|=
name|htable2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row not available"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
operator|*
name|NB_RETRIES
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertArrayEquals
argument_list|(
name|row
argument_list|,
name|res
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|fail
argument_list|(
literal|"Waited too much time for put replication"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

