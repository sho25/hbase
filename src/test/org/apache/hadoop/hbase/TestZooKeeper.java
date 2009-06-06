begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|client
operator|.
name|HBaseAdmin
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
name|HConnection
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
name|HConnectionManager
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
name|io
operator|.
name|BatchUpdate
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
name|master
operator|.
name|HMaster
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
name|zookeeper
operator|.
name|ZooKeeperWrapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|Watcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TestZooKeeper
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|static
class|class
name|EmptyWatcher
implements|implements
name|Watcher
block|{
specifier|public
name|EmptyWatcher
parameter_list|()
block|{}
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|event
parameter_list|)
block|{}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|setOpenMetaTable
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
comment|/**     * @throws IOException    */
specifier|public
name|void
name|testWritesRootRegionLocation
parameter_list|()
throws|throws
name|IOException
block|{
name|ZooKeeperWrapper
name|zooKeeper
init|=
operator|new
name|ZooKeeperWrapper
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|outOfSafeMode
init|=
name|zooKeeper
operator|.
name|checkOutOfSafeMode
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|outOfSafeMode
argument_list|)
expr_stmt|;
name|HServerAddress
name|zooKeeperRootAddress
init|=
name|zooKeeper
operator|.
name|readRootRegionLocation
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|zooKeeperRootAddress
argument_list|)
expr_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|HServerAddress
name|masterRootAddress
init|=
name|master
operator|.
name|getRootRegionLocation
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|masterRootAddress
argument_list|)
expr_stmt|;
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|outOfSafeMode
operator|=
name|zooKeeper
operator|.
name|checkOutOfSafeMode
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|outOfSafeMode
argument_list|)
expr_stmt|;
name|zooKeeperRootAddress
operator|=
name|zooKeeper
operator|.
name|readRootRegionLocation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|zooKeeperRootAddress
argument_list|)
expr_stmt|;
name|masterRootAddress
operator|=
name|master
operator|.
name|getRootRegionLocation
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|masterRootAddress
argument_list|,
name|zooKeeperRootAddress
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws IOException    */
specifier|public
name|void
name|testParentExists
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"zookeeper.znode.safemode"
argument_list|,
literal|"/a/b/c/d/e"
argument_list|)
expr_stmt|;
name|ZooKeeperWrapper
name|zooKeeper
init|=
operator|new
name|ZooKeeperWrapper
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|zooKeeper
operator|.
name|writeOutOfSafeMode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * See HBASE-1232 and http://wiki.apache.org/hadoop/ZooKeeper/FAQ#4.    * @throws IOException    * @throws InterruptedException    */
specifier|public
name|void
name|testClientSessionExpired
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|String
name|quorumServers
init|=
name|ZooKeeperWrapper
operator|.
name|getQuorumServers
argument_list|()
decl_stmt|;
name|int
name|sessionTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"zookeeper.session.timeout"
argument_list|,
literal|2
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|Watcher
name|watcher
init|=
operator|new
name|EmptyWatcher
argument_list|()
decl_stmt|;
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ZooKeeperWrapper
name|connectionZK
init|=
name|connection
operator|.
name|getZooKeeperWrapper
argument_list|()
decl_stmt|;
name|long
name|sessionID
init|=
name|connectionZK
operator|.
name|getSessionID
argument_list|()
decl_stmt|;
name|byte
index|[]
name|password
init|=
name|connectionZK
operator|.
name|getSessionPassword
argument_list|()
decl_stmt|;
name|ZooKeeper
name|zk
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|watcher
argument_list|,
name|sessionID
argument_list|,
name|password
argument_list|)
decl_stmt|;
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sessionTimeout
operator|*
literal|3
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ZooKeeper should have timed out"
argument_list|)
expr_stmt|;
name|connection
operator|.
name|relocateRegion
argument_list|(
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
block|}
comment|/**    *    */
specifier|public
name|void
name|testRegionServerSessionExpired
parameter_list|()
block|{
try|try
block|{
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|String
name|quorumServers
init|=
name|ZooKeeperWrapper
operator|.
name|getQuorumServers
argument_list|()
decl_stmt|;
name|int
name|sessionTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"zookeeper.session.timeout"
argument_list|,
literal|2
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|Watcher
name|watcher
init|=
operator|new
name|EmptyWatcher
argument_list|()
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ZooKeeperWrapper
name|rsZK
init|=
name|rs
operator|.
name|getZooKeeperWrapper
argument_list|()
decl_stmt|;
name|long
name|sessionID
init|=
name|rsZK
operator|.
name|getSessionID
argument_list|()
decl_stmt|;
name|byte
index|[]
name|password
init|=
name|rsZK
operator|.
name|getSessionPassword
argument_list|()
decl_stmt|;
name|ZooKeeper
name|zk
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
name|watcher
argument_list|,
name|sessionID
argument_list|,
name|password
argument_list|)
decl_stmt|;
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sessionTimeout
operator|*
literal|3
argument_list|)
expr_stmt|;
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|family
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"fam:"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow"
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testdata"
argument_list|)
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
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

