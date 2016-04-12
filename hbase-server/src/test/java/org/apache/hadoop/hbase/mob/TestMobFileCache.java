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
name|Date
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|regionserver
operator|.
name|HMobStore
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMobFileCache
extends|extends
name|TestCase
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
name|TestMobFileCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|UTIL
decl_stmt|;
specifier|private
name|HRegion
name|region
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|MobCacheConfig
name|mobCacheConf
decl_stmt|;
specifier|private
name|MobFileCache
name|mobFileCache
decl_stmt|;
specifier|private
name|Date
name|currentDate
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_CACHE_SIZE
init|=
literal|"2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EXPECTED_CACHE_SIZE_ZERO
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EXPECTED_CACHE_SIZE_ONE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EXPECTED_CACHE_SIZE_TWO
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|EXPECTED_CACHE_SIZE_THREE
init|=
literal|3
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
specifier|private
specifier|static
specifier|final
name|String
name|TABLE
init|=
literal|"tableName"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY1
init|=
literal|"family1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY2
init|=
literal|"family2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY3
init|=
literal|"family3"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
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
name|ROW2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QF1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QF2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QF3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf3"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
expr_stmt|;
name|conf
operator|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|UTIL
operator|.
name|createTableDescriptor
argument_list|(
literal|"testMobFileCache"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd1
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY1
argument_list|)
decl_stmt|;
name|hcd1
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd1
operator|.
name|setMobThreshold
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd2
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY2
argument_list|)
decl_stmt|;
name|hcd2
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd2
operator|.
name|setMobThreshold
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd3
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY3
argument_list|)
decl_stmt|;
name|hcd3
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd3
operator|.
name|setMobThreshold
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd1
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd2
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd3
argument_list|)
expr_stmt|;
name|region
operator|=
name|UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getFilesystem
argument_list|()
operator|.
name|delete
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create the mob store file.    */
specifier|private
name|Path
name|createMobStoreFile
parameter_list|(
name|String
name|family
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createMobStoreFile
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|family
argument_list|)
return|;
block|}
comment|/**    * Create the mob store file    */
specifier|private
name|Path
name|createMobStoreFile
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mobCacheConf
operator|=
operator|new
name|MobCacheConfig
argument_list|(
name|conf
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
return|return
name|createMobStoreFile
argument_list|(
name|hcd
argument_list|)
return|;
block|}
comment|/**    * Create the mob store file    */
specifier|private
name|Path
name|createMobStoreFile
parameter_list|(
name|HColumnDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Setting up a Store
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HMobStore
name|mobStore
init|=
operator|(
name|HMobStore
operator|)
name|region
operator|.
name|getStore
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|KeyValue
name|key1
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|QF1
argument_list|,
literal|1
argument_list|,
name|VALUE
argument_list|)
decl_stmt|;
name|KeyValue
name|key2
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|QF2
argument_list|,
literal|1
argument_list|,
name|VALUE
argument_list|)
decl_stmt|;
name|KeyValue
name|key3
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW2
argument_list|,
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|QF3
argument_list|,
literal|1
argument_list|,
name|VALUE2
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|keys
init|=
operator|new
name|KeyValue
index|[]
block|{
name|key1
block|,
name|key2
block|,
name|key3
block|}
decl_stmt|;
name|int
name|maxKeyCount
init|=
name|keys
operator|.
name|length
decl_stmt|;
name|HRegionInfo
name|regionInfo
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|StoreFileWriter
name|mobWriter
init|=
name|mobStore
operator|.
name|createWriterInTmp
argument_list|(
name|currentDate
argument_list|,
name|maxKeyCount
argument_list|,
name|hcd
operator|.
name|getCompactionCompression
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|mobFilePath
init|=
name|mobWriter
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|fileName
init|=
name|mobFilePath
operator|.
name|getName
argument_list|()
decl_stmt|;
name|mobWriter
operator|.
name|append
argument_list|(
name|key1
argument_list|)
expr_stmt|;
name|mobWriter
operator|.
name|append
argument_list|(
name|key2
argument_list|)
expr_stmt|;
name|mobWriter
operator|.
name|append
argument_list|(
name|key3
argument_list|)
expr_stmt|;
name|mobWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|targetPathName
init|=
name|MobUtils
operator|.
name|formatDate
argument_list|(
name|currentDate
argument_list|)
decl_stmt|;
name|Path
name|targetPath
init|=
operator|new
name|Path
argument_list|(
name|mobStore
operator|.
name|getPath
argument_list|()
argument_list|,
name|targetPathName
argument_list|)
decl_stmt|;
name|mobStore
operator|.
name|commitFile
argument_list|(
name|mobFilePath
argument_list|,
name|targetPath
argument_list|)
expr_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|targetPath
argument_list|,
name|fileName
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobFileCache
parameter_list|()
throws|throws
name|Exception
block|{
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
name|conf
operator|.
name|set
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_CACHE_SIZE_KEY
argument_list|,
name|TEST_CACHE_SIZE
argument_list|)
expr_stmt|;
name|mobFileCache
operator|=
operator|new
name|MobFileCache
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Path
name|file1Path
init|=
name|createMobStoreFile
argument_list|(
name|FAMILY1
argument_list|)
decl_stmt|;
name|Path
name|file2Path
init|=
name|createMobStoreFile
argument_list|(
name|FAMILY2
argument_list|)
decl_stmt|;
name|Path
name|file3Path
init|=
name|createMobStoreFile
argument_list|(
name|FAMILY3
argument_list|)
decl_stmt|;
comment|// Before open one file by the MobFileCache
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_ZERO
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Open one file by the MobFileCache
name|CachedMobFile
name|cachedMobFile1
init|=
operator|(
name|CachedMobFile
operator|)
name|mobFileCache
operator|.
name|openFile
argument_list|(
name|fs
argument_list|,
name|file1Path
argument_list|,
name|mobCacheConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_ONE
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|cachedMobFile1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile1
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// The evict is also managed by a schedule thread pool.
comment|// And its check period is set as 3600 seconds by default.
comment|// This evict should get the lock at the most time
name|mobFileCache
operator|.
name|evict
argument_list|()
expr_stmt|;
comment|// Cache not full, evict it
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_ONE
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile1
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|mobFileCache
operator|.
name|evictFile
argument_list|(
name|file1Path
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Evict one file
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_ZERO
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ONE
argument_list|,
name|cachedMobFile1
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|cachedMobFile1
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Close the cached mob file
comment|// Reopen three cached file
name|cachedMobFile1
operator|=
operator|(
name|CachedMobFile
operator|)
name|mobFileCache
operator|.
name|openFile
argument_list|(
name|fs
argument_list|,
name|file1Path
argument_list|,
name|mobCacheConf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_ONE
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|CachedMobFile
name|cachedMobFile2
init|=
operator|(
name|CachedMobFile
operator|)
name|mobFileCache
operator|.
name|openFile
argument_list|(
name|fs
argument_list|,
name|file2Path
argument_list|,
name|mobCacheConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_TWO
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|CachedMobFile
name|cachedMobFile3
init|=
operator|(
name|CachedMobFile
operator|)
name|mobFileCache
operator|.
name|openFile
argument_list|(
name|fs
argument_list|,
name|file3Path
argument_list|,
name|mobCacheConf
argument_list|)
decl_stmt|;
comment|// Before the evict
comment|// Evict the cache, should close the first file 1
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_THREE
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile1
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile2
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile3
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|mobFileCache
operator|.
name|evict
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_SIZE_ONE
argument_list|,
name|mobFileCache
operator|.
name|getCacheSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ONE
argument_list|,
name|cachedMobFile1
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_ONE
argument_list|,
name|cachedMobFile2
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_REFERENCE_TWO
argument_list|,
name|cachedMobFile3
operator|.
name|getReferenceCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

