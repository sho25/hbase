begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mob
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
name|Cell
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
name|KeyValue
operator|.
name|Type
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
name|hfile
operator|.
name|CacheConfig
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
name|hfile
operator|.
name|HFileContext
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
name|hfile
operator|.
name|HFileContextBuilder
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
name|StoreFileWriter
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
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCachedMobFile
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestCachedMobFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY1
init|=
literal|"familyName1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY2
init|=
literal|"familyName2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|EXPECTED_REFERENCE_ZERO
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|EXPECTED_REFERENCE_ONE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|EXPECTED_REFERENCE_TWO
init|=
literal|2
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testOpenClose
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|caseName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|Path
name|testDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|8
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
name|writer
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|testDir
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MobTestUtil
operator|.
name|writeStoreFile
argument_list|(
name|writer
argument_list|,
name|caseName
argument_list|)
expr_stmt|;
name|CachedMobFile
name|cachedMobFile
init|=
name|CachedMobFile
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|writer
operator|.
name|getPath
argument_list|()
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ZERO
argument_list|,
name|cachedMobFile
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|cachedMobFile
operator|.
name|open
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ONE
argument_list|,
name|cachedMobFile
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|cachedMobFile
operator|.
name|open
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|cachedMobFile
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ONE
argument_list|,
name|cachedMobFile
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|cachedMobFile
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ZERO
argument_list|,
name|cachedMobFile
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"SelfComparison"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testCompare
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|caseName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|Path
name|testDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|outputDir1
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
name|FAMILY1
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|8
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
name|writer1
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|outputDir1
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MobTestUtil
operator|.
name|writeStoreFile
argument_list|(
name|writer1
argument_list|,
name|caseName
argument_list|)
expr_stmt|;
name|CachedMobFile
name|cachedMobFile1
init|=
name|CachedMobFile
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|writer1
operator|.
name|getPath
argument_list|()
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|)
decl_stmt|;
name|Path
name|outputDir2
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
name|FAMILY2
argument_list|)
decl_stmt|;
name|StoreFileWriter
name|writer2
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|outputDir2
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MobTestUtil
operator|.
name|writeStoreFile
argument_list|(
name|writer2
argument_list|,
name|caseName
argument_list|)
expr_stmt|;
name|CachedMobFile
name|cachedMobFile2
init|=
name|CachedMobFile
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|writer2
operator|.
name|getPath
argument_list|()
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|)
decl_stmt|;
name|cachedMobFile1
operator|.
name|access
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|cachedMobFile2
operator|.
name|access
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|cachedMobFile1
operator|.
name|compareTo
argument_list|(
name|cachedMobFile2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|cachedMobFile2
operator|.
name|compareTo
argument_list|(
name|cachedMobFile1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cachedMobFile1
operator|.
name|compareTo
argument_list|(
name|cachedMobFile1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadKeyValue
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|testDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|8
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
name|writer
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|testDir
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|caseName
init|=
name|testName
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|MobTestUtil
operator|.
name|writeStoreFile
argument_list|(
name|writer
argument_list|,
name|caseName
argument_list|)
expr_stmt|;
name|CachedMobFile
name|cachedMobFile
init|=
name|CachedMobFile
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|writer
operator|.
name|getPath
argument_list|()
argument_list|,
name|conf
argument_list|,
name|cacheConf
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|caseName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualify
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|caseName
argument_list|)
decl_stmt|;
comment|// Test the start key
name|byte
index|[]
name|startKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aa"
argument_list|)
decl_stmt|;
comment|// The start key bytes
name|KeyValue
name|expectedKey
init|=
operator|new
name|KeyValue
argument_list|(
name|startKey
argument_list|,
name|family
argument_list|,
name|qualify
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|startKey
argument_list|)
decl_stmt|;
name|KeyValue
name|seekKey
init|=
name|expectedKey
operator|.
name|createKeyOnly
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Cell
name|cell
init|=
name|cachedMobFile
operator|.
name|readCell
argument_list|(
name|seekKey
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|MobTestUtil
operator|.
name|assertCellEquals
argument_list|(
name|expectedKey
argument_list|,
name|cell
argument_list|)
expr_stmt|;
comment|// Test the end key
name|byte
index|[]
name|endKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zz"
argument_list|)
decl_stmt|;
comment|// The end key bytes
name|expectedKey
operator|=
operator|new
name|KeyValue
argument_list|(
name|endKey
argument_list|,
name|family
argument_list|,
name|qualify
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|endKey
argument_list|)
expr_stmt|;
name|seekKey
operator|=
name|expectedKey
operator|.
name|createKeyOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cell
operator|=
name|cachedMobFile
operator|.
name|readCell
argument_list|(
name|seekKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MobTestUtil
operator|.
name|assertCellEquals
argument_list|(
name|expectedKey
argument_list|,
name|cell
argument_list|)
expr_stmt|;
comment|// Test the random key
name|byte
index|[]
name|randomKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|MobTestUtil
operator|.
name|generateRandomString
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|expectedKey
operator|=
operator|new
name|KeyValue
argument_list|(
name|randomKey
argument_list|,
name|family
argument_list|,
name|qualify
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|randomKey
argument_list|)
expr_stmt|;
name|seekKey
operator|=
name|expectedKey
operator|.
name|createKeyOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cell
operator|=
name|cachedMobFile
operator|.
name|readCell
argument_list|(
name|seekKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MobTestUtil
operator|.
name|assertCellEquals
argument_list|(
name|expectedKey
argument_list|,
name|cell
argument_list|)
expr_stmt|;
comment|// Test the key which is less than the start key
name|byte
index|[]
name|lowerKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a1"
argument_list|)
decl_stmt|;
comment|// Smaller than "aa"
name|expectedKey
operator|=
operator|new
name|KeyValue
argument_list|(
name|startKey
argument_list|,
name|family
argument_list|,
name|qualify
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|startKey
argument_list|)
expr_stmt|;
name|seekKey
operator|=
operator|new
name|KeyValue
argument_list|(
name|lowerKey
argument_list|,
name|family
argument_list|,
name|qualify
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|lowerKey
argument_list|)
expr_stmt|;
name|cell
operator|=
name|cachedMobFile
operator|.
name|readCell
argument_list|(
name|seekKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MobTestUtil
operator|.
name|assertCellEquals
argument_list|(
name|expectedKey
argument_list|,
name|cell
argument_list|)
expr_stmt|;
comment|// Test the key which is more than the end key
name|byte
index|[]
name|upperKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z{"
argument_list|)
decl_stmt|;
comment|// Bigger than "zz"
name|seekKey
operator|=
operator|new
name|KeyValue
argument_list|(
name|upperKey
argument_list|,
name|family
argument_list|,
name|qualify
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|upperKey
argument_list|)
expr_stmt|;
name|cell
operator|=
name|cachedMobFile
operator|.
name|readCell
argument_list|(
name|seekKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

