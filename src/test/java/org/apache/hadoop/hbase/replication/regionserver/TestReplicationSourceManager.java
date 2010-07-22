begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|HLogKey
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|replication
operator|.
name|ReplicationSourceDummy
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
name|replication
operator|.
name|ReplicationZookeeperWrapper
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
name|junit
operator|.
name|After
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
name|Before
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
name|java
operator|.
name|net
operator|.
name|URLEncoder
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
name|AtomicBoolean
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
name|assertEquals
import|;
end_import

begin_class
specifier|public
class|class
name|TestReplicationSourceManager
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
name|TestReplicationSourceManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|STOPPER
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|REPLICATING
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ReplicationSourceManager
name|manager
decl_stmt|;
specifier|private
specifier|static
name|ZooKeeperWrapper
name|zkw
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|r1
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
name|r2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f2"
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
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|oldLogDir
decl_stmt|;
specifier|private
specifier|static
name|Path
name|logDir
decl_stmt|;
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
name|set
argument_list|(
literal|"replication.replicationsource.implementation"
argument_list|,
name|ReplicationSourceDummy
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|utility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|utility
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|zkw
operator|=
name|ZooKeeperWrapper
operator|.
name|createInstance
argument_list|(
name|conf
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|zkw
operator|.
name|writeZNode
argument_list|(
literal|"/hbase"
argument_list|,
literal|"replication"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|zkw
operator|.
name|writeZNode
argument_list|(
literal|"/hbase/replication"
argument_list|,
literal|"master"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
operator|+
literal|":"
operator|+
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|)
operator|+
literal|":/1"
argument_list|)
expr_stmt|;
name|zkw
operator|.
name|writeZNode
argument_list|(
literal|"/hbase/replication/peers"
argument_list|,
literal|"1"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
operator|+
literal|":"
operator|+
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|)
operator|+
literal|":/1"
argument_list|)
expr_stmt|;
name|HRegionServer
name|server
init|=
operator|new
name|HRegionServer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ReplicationZookeeperWrapper
name|helper
init|=
operator|new
name|ReplicationZookeeperWrapper
argument_list|(
name|server
operator|.
name|getZooKeeperWrapper
argument_list|()
argument_list|,
name|conf
argument_list|,
name|REPLICATING
argument_list|,
literal|"123456789"
argument_list|)
decl_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|utility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|utility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
expr_stmt|;
name|manager
operator|=
operator|new
name|ReplicationSourceManager
argument_list|(
name|helper
argument_list|,
name|conf
argument_list|,
name|STOPPER
argument_list|,
name|fs
argument_list|,
name|REPLICATING
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|)
expr_stmt|;
name|manager
operator|.
name|addSource
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|test
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|col
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
name|col
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|col
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"f2"
argument_list|)
expr_stmt|;
name|col
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|hri
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
argument_list|,
name|r1
argument_list|,
name|r2
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
name|manager
operator|.
name|join
argument_list|()
expr_stmt|;
name|utility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|.
name|delete
argument_list|(
name|logDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|oldLogDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLogRoll
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|seq
init|=
literal|0
decl_stmt|;
name|long
name|baseline
init|=
literal|1000
decl_stmt|;
name|long
name|time
init|=
name|baseline
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|r1
argument_list|,
name|f1
argument_list|,
name|r1
argument_list|)
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|edit
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|HLog
name|hlog
init|=
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
name|manager
argument_list|,
name|URLEncoder
operator|.
name|encode
argument_list|(
literal|"regionserver:60020"
argument_list|,
literal|"UTF8"
argument_list|)
argument_list|)
decl_stmt|;
name|manager
operator|.
name|init
argument_list|()
expr_stmt|;
comment|// Testing normal log rolling every 20
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|101
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|1
operator|&&
name|i
operator|%
literal|20
operator|==
literal|0
condition|)
block|{
name|hlog
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|test
argument_list|,
name|seq
operator|++
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|)
expr_stmt|;
block|}
comment|// Simulate a rapid insert that's followed
comment|// by a report that's still not totally complete (missing last one)
name|LOG
operator|.
name|info
argument_list|(
name|baseline
operator|+
literal|" and "
operator|+
name|time
argument_list|)
expr_stmt|;
name|baseline
operator|+=
literal|101
expr_stmt|;
name|time
operator|=
name|baseline
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|baseline
operator|+
literal|" and "
operator|+
name|time
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|test
argument_list|,
name|seq
operator|++
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|manager
operator|.
name|getHLogs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|hlog
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|manager
operator|.
name|logPositionAndCleanOldLogs
argument_list|(
name|manager
operator|.
name|getSources
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCurrentPath
argument_list|()
argument_list|,
literal|"1"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|test
argument_list|,
name|seq
operator|++
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|key
argument_list|,
name|edit
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|manager
operator|.
name|getHLogs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO Need a case with only 2 HLogs and we only want to delete the first one
block|}
block|}
end_class

end_unit

