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
name|wal
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
name|Collections
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
name|FileStatus
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
name|client
operator|.
name|RegionInfo
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
name|RegionInfoBuilder
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
name|MultiVersionConcurrencyControl
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
name|TestWALRootDir
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
name|TestWALRootDir
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestWALRootDir
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|walFs
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
literal|"TestWALWALDir"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|rowName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"column"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Path
name|walRootDir
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootDir
decl_stmt|;
specifier|private
specifier|static
name|WALFactory
name|wals
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|cleanup
argument_list|()
expr_stmt|;
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|rootDir
operator|=
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
name|walRootDir
operator|=
name|TEST_UTIL
operator|.
name|createWALRootDir
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FSUtils
operator|.
name|getRootDirFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|walFs
operator|=
name|FSUtils
operator|.
name|getWALFileSystem
argument_list|(
name|conf
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
name|cleanup
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWALRootDir
parameter_list|()
throws|throws
name|Exception
block|{
name|RegionInfo
name|regionInfo
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|wals
operator|=
operator|new
name|WALFactory
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
literal|"testWALRootDir"
argument_list|)
expr_stmt|;
name|WAL
name|log
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|regionInfo
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|getWALFiles
argument_list|(
name|walFs
argument_list|,
name|walRootDir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|txid
init|=
name|log
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|getWalKey
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
literal|0
argument_list|)
argument_list|,
name|edit
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|log
operator|.
name|sync
argument_list|(
name|txid
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Expect 1 log have been created"
argument_list|,
literal|1
argument_list|,
name|getWALFiles
argument_list|(
name|walFs
argument_list|,
name|walRootDir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
comment|//Create 1 more WAL
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|getWALFiles
argument_list|(
name|walFs
argument_list|,
operator|new
name|Path
argument_list|(
name|walRootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2"
argument_list|)
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|txid
operator|=
name|log
operator|.
name|append
argument_list|(
name|regionInfo
argument_list|,
name|getWalKey
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
literal|1
argument_list|)
argument_list|,
name|edit
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|log
operator|.
name|sync
argument_list|(
name|txid
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|log
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Expect 3 logs in WALs dir"
argument_list|,
literal|3
argument_list|,
name|getWALFiles
argument_list|(
name|walFs
argument_list|,
operator|new
name|Path
argument_list|(
name|walRootDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|WALKeyImpl
name|getWalKey
parameter_list|(
specifier|final
name|long
name|time
parameter_list|,
name|RegionInfo
name|hri
parameter_list|,
specifier|final
name|long
name|startPoint
parameter_list|)
block|{
return|return
operator|new
name|WALKeyImpl
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|time
argument_list|,
operator|new
name|MultiVersionConcurrencyControl
argument_list|(
name|startPoint
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|FileStatus
argument_list|>
name|getWALFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|FileStatus
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Scanning "
operator|+
name|dir
operator|.
name|toString
argument_list|()
operator|+
literal|" for WAL files"
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
comment|// recurse into sub directories
name|result
operator|.
name|addAll
argument_list|(
name|getWALFiles
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|name
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|void
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|walFs
operator|.
name|delete
argument_list|(
name|walRootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

