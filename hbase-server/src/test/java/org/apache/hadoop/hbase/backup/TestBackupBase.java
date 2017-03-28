begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|backup
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Map
operator|.
name|Entry
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
name|LocatedFileStatus
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
name|fs
operator|.
name|RemoteIterator
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
name|NamespaceDescriptor
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
name|backup
operator|.
name|BackupInfo
operator|.
name|BackupState
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
name|backup
operator|.
name|impl
operator|.
name|BackupAdminImpl
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
name|backup
operator|.
name|impl
operator|.
name|BackupManager
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
name|backup
operator|.
name|impl
operator|.
name|BackupSystemTable
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
name|Connection
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
name|ConnectionFactory
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
name|Table
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
name|mapreduce
operator|.
name|HadoopSecurityEnabledUserProviderForTesting
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
name|UserProvider
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
name|access
operator|.
name|SecureTestUtil
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|wal
operator|.
name|WALFactory
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
name|MiniZooKeeperCluster
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

begin_comment
comment|/**  * This class is only a base for other integration-level backup tests. Do not add tests here.  * TestBackupSmallTests is where tests that don't require bring machines up/down should go All other  * tests should have their own classes and extend this one  */
end_comment

begin_class
specifier|public
class|class
name|TestBackupBase
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
name|TestBackupBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf1
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf2
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL2
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|HTableDescriptor
name|table1Desc
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table3
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table3"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table4
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table4"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table1_restore
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1:table1_restore"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table2_restore
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2:table2_restore"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table3_restore
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns3:table3_restore"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|table4_restore
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns4:table4_restore"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|NB_ROWS_IN_BATCH
init|=
literal|99
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|qualName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|famName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|String
name|BACKUP_ROOT_DIR
init|=
literal|"/backupUT"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|BACKUP_REMOTE_ROOT_DIR
init|=
literal|"/backupUT"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|provider
init|=
literal|"defaultProvider"
decl_stmt|;
specifier|protected
specifier|static
name|boolean
name|secure
init|=
literal|false
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
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
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|conf1
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
if|if
condition|(
name|secure
condition|)
block|{
comment|// set the always on security provider
name|UserProvider
operator|.
name|setUserProviderForTesting
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HadoopSecurityEnabledUserProviderForTesting
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// setup configuration
name|SecureTestUtil
operator|.
name|enableSecurity
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|coproc
init|=
name|conf1
operator|.
name|get
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|)
decl_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
operator|(
name|coproc
operator|==
literal|null
condition|?
literal|""
else|:
name|coproc
operator|+
literal|","
operator|)
operator|+
name|BackupObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|setBoolean
argument_list|(
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|BackupManager
operator|.
name|decorateMasterConfiguration
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|BackupManager
operator|.
name|decorateRegionServerConfiguration
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|conf1
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/1"
argument_list|)
expr_stmt|;
comment|// Set MultiWAL (with 2 default WAL files per RS)
name|conf1
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
name|provider
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniZooKeeperCluster
name|miniZK
init|=
name|TEST_UTIL
operator|.
name|getZkCluster
argument_list|()
decl_stmt|;
name|conf2
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
literal|"/2"
argument_list|)
expr_stmt|;
name|TEST_UTIL2
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|TEST_UTIL2
operator|.
name|setZkCluster
argument_list|(
name|miniZK
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL2
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|conf1
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|BACKUP_ROOT_DIR
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"fs.defaultFS"
argument_list|)
operator|+
literal|"/backupUT"
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ROOTDIR "
operator|+
name|BACKUP_ROOT_DIR
argument_list|)
expr_stmt|;
name|BACKUP_REMOTE_ROOT_DIR
operator|=
name|TEST_UTIL2
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"fs.defaultFS"
argument_list|)
operator|+
literal|"/backupUT"
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"REMOTE ROOTDIR "
operator|+
name|BACKUP_REMOTE_ROOT_DIR
argument_list|)
expr_stmt|;
name|createTables
argument_list|()
expr_stmt|;
name|populateFromMasterConfig
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|conf1
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|populateFromMasterConfig
parameter_list|(
name|Configuration
name|masterConf
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|it
init|=
name|masterConf
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @throws java.lang.Exception    */
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
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|TEST_UTIL
argument_list|)
expr_stmt|;
name|TEST_UTIL2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
name|HTable
name|insertIntoTable
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|TableName
name|table
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|int
name|id
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
operator|(
name|HTable
operator|)
name|conn
operator|.
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|Put
name|p1
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
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|p1
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row-"
operator|+
name|table
operator|+
literal|"-"
operator|+
name|id
operator|+
literal|"-"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|p1
operator|.
name|addColumn
argument_list|(
name|family
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
name|t
operator|.
name|put
argument_list|(
name|p1
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
specifier|protected
name|BackupRequest
name|createBackupRequest
parameter_list|(
name|BackupType
name|type
parameter_list|,
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|BackupRequest
operator|.
name|Builder
name|builder
init|=
operator|new
name|BackupRequest
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|BackupRequest
name|request
init|=
name|builder
operator|.
name|withBackupType
argument_list|(
name|type
argument_list|)
operator|.
name|withTableList
argument_list|(
name|tables
argument_list|)
operator|.
name|withTargetRootDir
argument_list|(
name|path
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|request
return|;
block|}
specifier|protected
name|String
name|backupTables
parameter_list|(
name|BackupType
name|type
parameter_list|,
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|BackupAdmin
name|badmin
init|=
literal|null
decl_stmt|;
name|String
name|backupId
decl_stmt|;
try|try
block|{
name|conn
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
expr_stmt|;
name|badmin
operator|=
operator|new
name|BackupAdminImpl
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|BackupRequest
name|request
init|=
name|createBackupRequest
argument_list|(
name|type
argument_list|,
name|tables
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|backupId
operator|=
name|badmin
operator|.
name|backupTables
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|badmin
operator|!=
literal|null
condition|)
block|{
name|badmin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|conn
operator|!=
literal|null
condition|)
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|backupId
return|;
block|}
specifier|protected
name|String
name|fullTableBackup
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|backupTables
argument_list|(
name|BackupType
operator|.
name|FULL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
return|;
block|}
specifier|protected
name|String
name|incrementalTableBackup
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|backupTables
argument_list|(
name|BackupType
operator|.
name|INCREMENTAL
argument_list|,
name|tables
argument_list|,
name|BACKUP_ROOT_DIR
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|void
name|loadTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|Exception
block|{
name|Put
name|p
decl_stmt|;
comment|// 100 + 1 row to t1_syncup
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
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
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
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|createTables
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|tid
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|table1
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1:test-"
operator|+
name|tid
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|ha
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// Create namespaces
name|NamespaceDescriptor
name|desc1
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"ns1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NamespaceDescriptor
name|desc2
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"ns2"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NamespaceDescriptor
name|desc3
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"ns3"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NamespaceDescriptor
name|desc4
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"ns4"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ha
operator|.
name|createNamespace
argument_list|(
name|desc1
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createNamespace
argument_list|(
name|desc2
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createNamespace
argument_list|(
name|desc3
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createNamespace
argument_list|(
name|desc4
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|famName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table1Desc
operator|=
name|desc
expr_stmt|;
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|table1
argument_list|)
decl_stmt|;
name|loadTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|table2
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns2:test-"
operator|+
name|tid
operator|+
literal|1
argument_list|)
expr_stmt|;
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|table2
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|ha
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table
operator|=
name|conn
operator|.
name|getTable
argument_list|(
name|table2
argument_list|)
expr_stmt|;
name|loadTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|table3
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns3:test-"
operator|+
name|tid
operator|+
literal|2
argument_list|)
expr_stmt|;
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table3
argument_list|,
name|famName
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|table4
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns4:test-"
operator|+
name|tid
operator|+
literal|3
argument_list|)
expr_stmt|;
name|table
operator|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|table4
argument_list|,
name|famName
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|ha
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|boolean
name|checkSucceeded
parameter_list|(
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
block|{
name|BackupInfo
name|status
init|=
name|getBackupInfo
argument_list|(
name|backupId
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|status
operator|.
name|getState
argument_list|()
operator|==
name|BackupState
operator|.
name|COMPLETE
return|;
block|}
specifier|protected
name|boolean
name|checkFailed
parameter_list|(
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
block|{
name|BackupInfo
name|status
init|=
name|getBackupInfo
argument_list|(
name|backupId
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|status
operator|.
name|getState
argument_list|()
operator|==
name|BackupState
operator|.
name|FAILED
return|;
block|}
specifier|private
name|BackupInfo
name|getBackupInfo
parameter_list|(
name|String
name|backupId
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|BackupSystemTable
name|table
init|=
operator|new
name|BackupSystemTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|)
init|)
block|{
name|BackupInfo
name|status
init|=
name|table
operator|.
name|readBackupInfo
argument_list|(
name|backupId
argument_list|)
decl_stmt|;
return|return
name|status
return|;
block|}
block|}
specifier|protected
name|BackupAdmin
name|getBackupAdmin
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|BackupAdminImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Helper method    */
specifier|protected
name|List
argument_list|<
name|TableName
argument_list|>
name|toList
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
block|{
name|List
argument_list|<
name|TableName
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|protected
name|void
name|dumpBackupDir
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Dump Backup Dir
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf1
argument_list|)
decl_stmt|;
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|it
init|=
name|fs
operator|.
name|listFiles
argument_list|(
operator|new
name|Path
argument_list|(
name|BACKUP_ROOT_DIR
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|it
operator|.
name|next
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

