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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ClusterMetrics
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
name|ClusterMetrics
operator|.
name|Option
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
name|HBaseClassTestRule
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
name|ServerMetrics
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
name|ServerName
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
name|Admin
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
name|testclassification
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|JVMClusterUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ReplicationTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReplicationStatus
extends|extends
name|TestReplicationBase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestReplicationStatus
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Test for HBASE-9531.    *<p/>    * put a few rows into htable1, which should be replicated to htable2<br/>    * create a ClusterStatus instance 'status' from HBaseAdmin<br/>    * test : status.getLoad(server).getReplicationLoadSourceList()<br/>    * test : status.getLoad(server).getReplicationLoadSink()    */
annotation|@
name|Test
specifier|public
name|void
name|testReplicationStatus
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|hbaseAdmin
init|=
name|utility1
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// disable peer
name|hbaseAdmin
operator|.
name|disableReplicationPeer
argument_list|(
name|PEER_ID2
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|qualName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
name|Put
name|p
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
name|NB_ROWS_IN_BATCH
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|famName
argument_list|,
name|qualName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|htable1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|ClusterMetrics
name|metrics
init|=
name|hbaseAdmin
operator|.
name|getClusterMetrics
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|thread
range|:
name|utility1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|ServerName
name|server
init|=
name|thread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|ServerMetrics
name|sm
init|=
name|metrics
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ReplicationLoadSource
argument_list|>
name|rLoadSourceList
init|=
name|sm
operator|.
name|getReplicationLoadSourceList
argument_list|()
decl_stmt|;
name|ReplicationLoadSink
name|rLoadSink
init|=
name|sm
operator|.
name|getReplicationLoadSink
argument_list|()
decl_stmt|;
comment|// check SourceList only has one entry, because only has one peer
name|assertTrue
argument_list|(
literal|"failed to get ReplicationLoadSourceList"
argument_list|,
operator|(
name|rLoadSourceList
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PEER_ID2
argument_list|,
name|rLoadSourceList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPeerID
argument_list|()
argument_list|)
expr_stmt|;
comment|// check Sink exist only as it is difficult to verify the value on the fly
name|assertTrue
argument_list|(
literal|"failed to get ReplicationLoadSink.AgeOfLastShippedOp "
argument_list|,
operator|(
name|rLoadSink
operator|.
name|getAgeOfLastAppliedOp
argument_list|()
operator|>=
literal|0
operator|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"failed to get ReplicationLoadSink.TimeStampsOfLastAppliedOp "
argument_list|,
operator|(
name|rLoadSink
operator|.
name|getTimestampsOfLastAppliedOp
argument_list|()
operator|>=
literal|0
operator|)
argument_list|)
expr_stmt|;
block|}
comment|// Stop rs1, then the queue of rs1 will be transfered to rs0
name|utility1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
operator|.
name|stop
argument_list|(
literal|"Stop RegionServer"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|metrics
operator|=
name|hbaseAdmin
operator|.
name|getClusterMetrics
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|Option
operator|.
name|LIVE_SERVERS
argument_list|)
argument_list|)
expr_stmt|;
name|ServerName
name|server
init|=
name|utility1
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|ServerMetrics
name|sm
init|=
name|metrics
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ReplicationLoadSource
argument_list|>
name|rLoadSourceList
init|=
name|sm
operator|.
name|getReplicationLoadSourceList
argument_list|()
decl_stmt|;
comment|// check SourceList still only has one entry
name|assertTrue
argument_list|(
literal|"failed to get ReplicationLoadSourceList"
argument_list|,
operator|(
name|rLoadSourceList
operator|.
name|size
argument_list|()
operator|==
literal|2
operator|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PEER_ID2
argument_list|,
name|rLoadSourceList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPeerID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

