begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|migration
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|FileUtil
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
name|FsShell
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
name|Waiter
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
name|client
operator|.
name|ResultScanner
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
name|Scan
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
name|AdminProtos
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
name|security
operator|.
name|access
operator|.
name|AccessControlLists
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
name|util
operator|.
name|ToolRunner
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

begin_comment
comment|/**  * Test upgrade from no namespace in 0.94 to namespace directory structure.  * Mainly tests that tables are migrated and consistent. Also verifies  * that snapshots have been migrated correctly.  *  * Uses a tarball which is an image of an 0.94 hbase.rootdir.  *  * Contains tables with currentKeys as the stored keys:  * foo, ns1.foo, ns2.foo  *  * Contains snapshots with snapshot{num}Keys as the contents:  * snapshot1Keys, snapshot2Keys  *  * Image also contains _acl_ table with one region and two storefiles.  * This is needed to test the acl table migration.  *  */
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
name|TestNamespaceUpgrade
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestNamespaceUpgrade
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
specifier|final
specifier|static
name|String
name|snapshot1Keys
index|[]
init|=
block|{
literal|"1"
block|,
literal|"10"
block|,
literal|"2"
block|,
literal|"3"
block|,
literal|"4"
block|,
literal|"5"
block|,
literal|"6"
block|,
literal|"7"
block|,
literal|"8"
block|,
literal|"9"
block|}
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|snapshot2Keys
index|[]
init|=
block|{
literal|"1"
block|,
literal|"2"
block|,
literal|"3"
block|,
literal|"4"
block|,
literal|"5"
block|,
literal|"6"
block|,
literal|"7"
block|,
literal|"8"
block|,
literal|"9"
block|}
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|currentKeys
index|[]
init|=
block|{
literal|"1"
block|,
literal|"2"
block|,
literal|"3"
block|,
literal|"4"
block|,
literal|"5"
block|,
literal|"6"
block|,
literal|"7"
block|,
literal|"8"
block|,
literal|"9"
block|,
literal|"A"
block|}
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|tables
index|[]
init|=
block|{
literal|"foo"
block|,
literal|"ns1.foo"
block|,
literal|"ns.two.foo"
block|}
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
comment|// Start up our mini cluster on top of an 0.94 root.dir that has data from
comment|// a 0.94 hbase run and see if we can migrate to 0.96
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Path
name|testdir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestNamespaceUpgrade"
argument_list|)
decl_stmt|;
comment|// Untar our test dir.
name|File
name|untar
init|=
name|untar
argument_list|(
operator|new
name|File
argument_list|(
name|testdir
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// Now copy the untar up into hdfs so when we start hbase, we'll run from it.
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|FsShell
name|shell
init|=
operator|new
name|FsShell
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// find where hbase will root itself, so we can copy filesystem there
name|Path
name|hbaseRootDir
init|=
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|isDirectory
argument_list|(
name|hbaseRootDir
operator|.
name|getParent
argument_list|()
argument_list|)
condition|)
block|{
comment|// mkdir at first
name|fs
operator|.
name|mkdirs
argument_list|(
name|hbaseRootDir
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|doFsCommand
argument_list|(
name|shell
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-put"
block|,
name|untar
operator|.
name|toURI
argument_list|()
operator|.
name|toString
argument_list|()
block|,
name|hbaseRootDir
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
comment|// See whats in minihdfs.
name|doFsCommand
argument_list|(
name|shell
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-lsr"
block|,
literal|"/"
block|}
argument_list|)
expr_stmt|;
name|Configuration
name|toolConf
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
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|toolConf
argument_list|,
operator|new
name|NamespaceUpgrade
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"--upgrade"
block|}
argument_list|)
expr_stmt|;
name|doFsCommand
argument_list|(
name|shell
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-lsr"
block|,
literal|"/"
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|FSUtils
operator|.
name|getVersion
argument_list|(
name|fs
argument_list|,
name|hbaseRootDir
argument_list|)
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|table
range|:
name|tables
control|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
name|currentKeys
index|[
name|count
operator|++
index|]
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|currentKeys
operator|.
name|length
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listNamespaceDescriptors
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//verify ACL table is migrated
name|HTable
name|secureTable
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|AccessControlLists
operator|.
name|ACL_TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|secureTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|scanner
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
literal|"_acl_"
argument_list|)
argument_list|)
expr_stmt|;
comment|//verify ACL table was compacted
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|secureTable
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getStores
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|File
name|untar
parameter_list|(
specifier|final
name|File
name|testdir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Find the src data under src/test/data
specifier|final
name|String
name|datafile
init|=
literal|"TestNamespaceUpgrade"
decl_stmt|;
name|File
name|srcTarFile
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"project.build.testSourceDirectory"
argument_list|,
literal|"src/test"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
literal|"data"
operator|+
name|File
operator|.
name|separator
operator|+
name|datafile
operator|+
literal|".tgz"
argument_list|)
decl_stmt|;
name|File
name|homedir
init|=
operator|new
name|File
argument_list|(
name|testdir
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|File
name|tgtUntarDir
init|=
operator|new
name|File
argument_list|(
name|homedir
argument_list|,
literal|"hbase"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tgtUntarDir
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|tgtUntarDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed delete of "
operator|+
name|tgtUntarDir
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|srcTarFile
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|srcTarFile
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Untarring "
operator|+
name|srcTarFile
operator|+
literal|" into "
operator|+
name|homedir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|FileUtil
operator|.
name|unTar
argument_list|(
name|srcTarFile
argument_list|,
name|homedir
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tgtUntarDir
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tgtUntarDir
return|;
block|}
specifier|private
specifier|static
name|void
name|doFsCommand
parameter_list|(
specifier|final
name|FsShell
name|shell
parameter_list|,
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Run the 'put' command.
name|int
name|errcode
init|=
name|shell
operator|.
name|run
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|errcode
operator|!=
literal|0
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed put; errcode="
operator|+
name|errcode
argument_list|)
throw|;
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSnapshots
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|snapshots
index|[]
index|[]
init|=
block|{
name|snapshot1Keys
block|,
name|snapshot2Keys
block|}
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|snapshots
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|String
name|table
range|:
name|tables
control|)
block|{
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|cloneSnapshot
argument_list|(
name|table
operator|+
literal|"_snapshot"
operator|+
name|i
argument_list|,
name|table
operator|+
literal|"_clone"
operator|+
name|i
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
operator|+
literal|"_clone"
operator|+
name|i
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
name|snapshots
index|[
name|i
operator|-
literal|1
index|]
index|[
name|count
operator|++
index|]
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|table
operator|+
literal|"_snapshot"
operator|+
name|i
argument_list|,
name|snapshots
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|length
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRenameUsingSnapshots
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|newNS
init|=
literal|"newNS"
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|newNS
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|table
range|:
name|tables
control|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
name|currentKeys
index|[
name|count
operator|++
index|]
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|snapshot
argument_list|(
name|table
operator|+
literal|"_snapshot3"
argument_list|,
name|table
argument_list|)
expr_stmt|;
specifier|final
name|String
name|newTableName
init|=
name|newNS
operator|+
name|TableName
operator|.
name|NAMESPACE_DELIM
operator|+
name|table
operator|+
literal|"_clone3"
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|cloneSnapshot
argument_list|(
name|table
operator|+
literal|"_snapshot3"
argument_list|,
name|newTableName
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|count
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Result
name|res
range|:
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|newTableName
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
name|currentKeys
index|[
name|count
operator|++
index|]
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|newTableName
argument_list|,
name|currentKeys
operator|.
name|length
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|newTableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|majorCompact
argument_list|(
name|newTableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|2000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|IOException
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getCompactionState
argument_list|(
name|newTableName
argument_list|)
operator|==
name|AdminProtos
operator|.
name|GetRegionInfoResponse
operator|.
name|CompactionState
operator|.
name|NONE
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|String
name|nextNS
init|=
literal|"nextNS"
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|nextNS
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|table
range|:
name|tables
control|)
block|{
name|String
name|srcTable
init|=
name|newNS
operator|+
name|TableName
operator|.
name|NAMESPACE_DELIM
operator|+
name|table
operator|+
literal|"_clone3"
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|snapshot
argument_list|(
name|table
operator|+
literal|"_snapshot4"
argument_list|,
name|srcTable
argument_list|)
expr_stmt|;
name|String
name|newTableName
init|=
name|nextNS
operator|+
name|TableName
operator|.
name|NAMESPACE_DELIM
operator|+
name|table
operator|+
literal|"_clone4"
decl_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|cloneSnapshot
argument_list|(
name|table
operator|+
literal|"_snapshot4"
argument_list|,
name|newTableName
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|newTableName
argument_list|)
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
name|currentKeys
index|[
name|count
operator|++
index|]
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|newTableName
argument_list|,
name|currentKeys
operator|.
name|length
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

