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
name|HRegion
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
name|HLogFactory
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
name|HLogSplitter
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
name|WALCoprocessorHost
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
name|security
operator|.
name|User
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
name|FSUtils
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
name|EnvironmentEdge
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
name|EnvironmentEdgeManager
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
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
comment|/**  * Tests invocation of the  * {@link org.apache.hadoop.hbase.coprocessor.MasterObserver} interface hooks at  * all appropriate times during normal HMaster operations.  */
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
name|TestWALObserver
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
name|TestWALObserver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"observedTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|TEST_FAMILY
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam3"
argument_list|)
block|, }
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|TEST_QUALIFIER
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q3"
argument_list|)
block|, }
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|TEST_VALUE
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v3"
argument_list|)
block|, }
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|dir
decl_stmt|;
specifier|private
name|Path
name|hbaseRootDir
decl_stmt|;
specifier|private
name|String
name|logName
decl_stmt|;
specifier|private
name|Path
name|oldLogDir
decl_stmt|;
specifier|private
name|Path
name|logDir
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
name|WAL_COPROCESSOR_CONF_KEY
argument_list|,
name|SampleRegionWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|SampleRegionWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.client.block.recovery.retries"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Path
name|hbaseRootDir
init|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/hbase"
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"hbase.rootdir="
operator|+
name|hbaseRootDir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setRootDir
argument_list|(
name|conf
argument_list|,
name|hbaseRootDir
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// this.cluster = TEST_UTIL.getDFSCluster();
name|this
operator|.
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|this
operator|.
name|hbaseRootDir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|dir
operator|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
name|TestWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|logDir
operator|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|logName
operator|=
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
expr_stmt|;
if|if
condition|(
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|exists
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
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
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test WAL write behavior with WALObserver. The coprocessor monitors a    * WALEdit written to WAL, and ignore, modify, and add KeyValue's for the    * WALEdit.    */
annotation|@
name|Test
specifier|public
name|void
name|testWALObserverWriteToWAL
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionInfo
name|hri
init|=
name|createBasic3FamilyHRegionInfo
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|createBasic3FamilyHTD
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|basedir
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
decl_stmt|;
name|deleteDir
argument_list|(
name|basedir
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|basedir
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HLog
name|log
init|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|hbaseRootDir
argument_list|,
name|TestWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|SampleRegionWALObserver
name|cp
init|=
name|getCoprocessor
argument_list|(
name|log
argument_list|)
decl_stmt|;
comment|// TEST_FAMILY[0] shall be removed from WALEdit.
comment|// TEST_FAMILY[1] value shall be changed.
comment|// TEST_FAMILY[2] shall be added to WALEdit, although it's not in the put.
name|cp
operator|.
name|setTestValues
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_ROW
argument_list|,
name|TEST_FAMILY
index|[
literal|0
index|]
argument_list|,
name|TEST_QUALIFIER
index|[
literal|0
index|]
argument_list|,
name|TEST_FAMILY
index|[
literal|1
index|]
argument_list|,
name|TEST_QUALIFIER
index|[
literal|1
index|]
argument_list|,
name|TEST_FAMILY
index|[
literal|2
index|]
argument_list|,
name|TEST_QUALIFIER
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cp
operator|.
name|isPreWALWriteCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cp
operator|.
name|isPostWALWriteCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// TEST_FAMILY[2] is not in the put, however it shall be added by the tested
comment|// coprocessor.
comment|// Use a Put to create familyMap.
name|Put
name|p
init|=
name|creatPutWith2Families
argument_list|(
name|TEST_ROW
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|familyMap
init|=
name|p
operator|.
name|getFamilyCellMap
argument_list|()
decl_stmt|;
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|addFamilyMapToWALEdit
argument_list|(
name|familyMap
argument_list|,
name|edit
argument_list|)
expr_stmt|;
name|boolean
name|foundFamily0
init|=
literal|false
decl_stmt|;
name|boolean
name|foundFamily2
init|=
literal|false
decl_stmt|;
name|boolean
name|modifiedFamily1
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|edit
operator|.
name|getKeyValues
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|TEST_FAMILY
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|foundFamily0
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|TEST_FAMILY
index|[
literal|2
index|]
argument_list|)
condition|)
block|{
name|foundFamily2
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|TEST_FAMILY
index|[
literal|1
index|]
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|,
name|TEST_VALUE
index|[
literal|1
index|]
argument_list|)
condition|)
block|{
name|modifiedFamily1
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
name|foundFamily0
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|foundFamily2
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|modifiedFamily1
argument_list|)
expr_stmt|;
comment|// it's where WAL write cp should occur.
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|log
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|hri
operator|.
name|getTableName
argument_list|()
argument_list|,
name|edit
argument_list|,
name|now
argument_list|,
name|htd
argument_list|)
expr_stmt|;
comment|// the edit shall have been change now by the coprocessor.
name|foundFamily0
operator|=
literal|false
expr_stmt|;
name|foundFamily2
operator|=
literal|false
expr_stmt|;
name|modifiedFamily1
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|TEST_FAMILY
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|foundFamily0
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|TEST_FAMILY
index|[
literal|2
index|]
argument_list|)
condition|)
block|{
name|foundFamily2
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|TEST_FAMILY
index|[
literal|1
index|]
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|,
name|TEST_VALUE
index|[
literal|1
index|]
argument_list|)
condition|)
block|{
name|modifiedFamily1
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
name|assertFalse
argument_list|(
name|foundFamily0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundFamily2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|modifiedFamily1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cp
operator|.
name|isPreWALWriteCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cp
operator|.
name|isPostWALWriteCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test WAL replay behavior with WALObserver.    */
annotation|@
name|Test
specifier|public
name|void
name|testWALCoprocessorReplay
parameter_list|()
throws|throws
name|Exception
block|{
comment|// WAL replay is handled at HRegion::replayRecoveredEdits(), which is
comment|// ultimately called by HRegion::initialize()
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testWALCoprocessorReplay"
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
name|getBasic3FamilyHTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// final HRegionInfo hri =
comment|// createBasic3FamilyHRegionInfo(Bytes.toString(tableName));
comment|// final HRegionInfo hri1 =
comment|// createBasic3FamilyHRegionInfo(Bytes.toString(tableName));
specifier|final
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|basedir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|this
operator|.
name|hbaseRootDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|deleteDir
argument_list|(
name|basedir
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|basedir
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Configuration
name|newConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
comment|// HLog wal = new HLog(this.fs, this.dir, this.oldLogDir, this.conf);
name|HLog
name|wal
init|=
name|createWAL
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
comment|// Put p = creatPutWith2Families(TEST_ROW);
name|WALEdit
name|edit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// addFamilyMapToWALEdit(p.getFamilyMap(), edit);
specifier|final
name|int
name|countPerFamily
init|=
literal|1000
decl_stmt|;
comment|// for (HColumnDescriptor hcd: hri.getTableDesc().getFamilies()) {
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|htd
operator|.
name|getFamilies
argument_list|()
control|)
block|{
comment|// addWALEdits(tableName, hri, TEST_ROW, hcd.getName(), countPerFamily,
comment|// EnvironmentEdgeManager.getDelegate(), wal);
name|addWALEdits
argument_list|(
name|tableName
argument_list|,
name|hri
argument_list|,
name|TEST_ROW
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|countPerFamily
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|getDelegate
argument_list|()
argument_list|,
name|wal
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
name|wal
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|now
argument_list|,
name|htd
argument_list|)
expr_stmt|;
comment|// sync to fs.
name|wal
operator|.
name|sync
argument_list|()
expr_stmt|;
name|User
name|user
init|=
name|HBaseTestingUtility
operator|.
name|getDifferentUser
argument_list|(
name|newConf
argument_list|,
literal|".replay.wal.secondtime"
argument_list|)
decl_stmt|;
name|user
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|p
init|=
name|runWALSplit
argument_list|(
name|newConf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"WALSplit path == "
operator|+
name|p
argument_list|)
expr_stmt|;
name|FileSystem
name|newFS
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|newConf
argument_list|)
decl_stmt|;
comment|// Make a new wal for new region open.
name|HLog
name|wal2
init|=
name|createWAL
argument_list|(
name|newConf
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|newConf
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|newConf
argument_list|)
argument_list|,
name|hbaseRootDir
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
name|wal2
argument_list|,
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|long
name|seqid2
init|=
name|region
operator|.
name|getOpenSeqNum
argument_list|()
decl_stmt|;
name|SampleRegionWALObserver
name|cp2
init|=
operator|(
name|SampleRegionWALObserver
operator|)
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|SampleRegionWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO: asserting here is problematic.
name|assertNotNull
argument_list|(
name|cp2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cp2
operator|.
name|isPreWALRestoreCalled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cp2
operator|.
name|isPostWALRestoreCalled
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|wal2
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test to see CP loaded successfully or not. There is a duplication at    * TestHLog, but the purpose of that one is to see whether the loaded CP will    * impact existing HLog tests or not.    */
annotation|@
name|Test
specifier|public
name|void
name|testWALObserverLoaded
parameter_list|()
throws|throws
name|Exception
block|{
name|HLog
name|log
init|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|hbaseRootDir
argument_list|,
name|TestWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|getCoprocessor
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|SampleRegionWALObserver
name|getCoprocessor
parameter_list|(
name|HLog
name|wal
parameter_list|)
throws|throws
name|Exception
block|{
name|WALCoprocessorHost
name|host
init|=
name|wal
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|Coprocessor
name|c
init|=
name|host
operator|.
name|findCoprocessor
argument_list|(
name|SampleRegionWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|(
name|SampleRegionWALObserver
operator|)
name|c
return|;
block|}
comment|/*    * Creates an HRI around an HTD that has<code>tableName</code> and three    * column families named.    *     * @param tableName Name of table to use when we create HTableDescriptor.    */
specifier|private
name|HRegionInfo
name|createBasic3FamilyHRegionInfo
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
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
name|TEST_FAMILY
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|HColumnDescriptor
name|a
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/*    * @param p Directory to cleanup    */
specifier|private
name|void
name|deleteDir
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed remove of "
operator|+
name|p
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|Put
name|creatPutWith2Families
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
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
name|TEST_FAMILY
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|p
operator|.
name|add
argument_list|(
name|TEST_FAMILY
index|[
name|i
index|]
argument_list|,
name|TEST_QUALIFIER
index|[
name|i
index|]
argument_list|,
name|TEST_VALUE
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
comment|/**    * Copied from HRegion.    *     * @param familyMap    *          map of family->edits    * @param walEdit    *          the destination entry to append into    */
specifier|private
name|void
name|addFamilyMapToWALEdit
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
block|{
for|for
control|(
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|edits
range|:
name|familyMap
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|edits
control|)
block|{
comment|// KeyValue v1 expectation. Cast for now until we go all Cell all the time. TODO.
name|walEdit
operator|.
name|add
argument_list|(
operator|(
name|KeyValue
operator|)
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Path
name|runWALSplit
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|splits
init|=
name|HLogSplitter
operator|.
name|split
argument_list|(
name|hbaseRootDir
argument_list|,
name|logDir
argument_list|,
name|oldLogDir
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
argument_list|,
name|c
argument_list|)
decl_stmt|;
comment|// Split should generate only 1 file since there's only 1 region
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure the file exists
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Split file="
operator|+
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|private
name|HLog
name|createWAL
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
argument_list|,
name|hbaseRootDir
argument_list|,
name|logName
argument_list|,
name|c
argument_list|)
return|;
block|}
specifier|private
name|void
name|addWALEdits
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
specifier|final
name|byte
index|[]
name|rowName
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
name|EnvironmentEdge
name|ee
parameter_list|,
specifier|final
name|HLog
name|wal
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|familyStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|count
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|qualifierBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|columnBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familyStr
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
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
operator|new
name|KeyValue
argument_list|(
name|rowName
argument_list|,
name|family
argument_list|,
name|qualifierBytes
argument_list|,
name|ee
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|columnBytes
argument_list|)
argument_list|)
expr_stmt|;
name|wal
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|,
name|ee
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HTableDescriptor
name|getBasic3FamilyHTableDescriptor
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
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
name|TEST_FAMILY
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|HColumnDescriptor
name|a
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
return|return
name|htd
return|;
block|}
specifier|private
name|HTableDescriptor
name|createBasic3FamilyHTD
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|a
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|b
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|c
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|c
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
block|}
end_class

end_unit

