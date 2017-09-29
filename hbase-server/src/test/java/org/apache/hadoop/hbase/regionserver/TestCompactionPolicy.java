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
name|regionserver
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
name|compactions
operator|.
name|CompactionConfiguration
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
name|compactions
operator|.
name|CompactionRequestImpl
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
name|compactions
operator|.
name|RatioBasedCompactionPolicy
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
name|FSHLog
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
name|After
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
name|Before
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestCompactionPolicy
block|{
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|HStore
name|store
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
name|TestCompactionPolicy
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|Path
name|TEST_FILE
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|minFiles
init|=
literal|3
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|maxFiles
init|=
literal|5
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|minSize
init|=
literal|10
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|maxSize
init|=
literal|2100
decl_stmt|;
specifier|private
name|FSHLog
name|hlog
decl_stmt|;
specifier|private
name|HRegion
name|region
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
name|config
argument_list|()
expr_stmt|;
name|initialize
argument_list|()
expr_stmt|;
block|}
comment|/**    * setup config values necessary for store    */
specifier|protected
name|void
name|config
parameter_list|()
block|{
name|this
operator|.
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MIN_KEY
argument_list|,
name|minFiles
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MAX_KEY
argument_list|,
name|maxFiles
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MIN_SIZE_KEY
argument_list|,
name|minSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MAX_SIZE_KEY
argument_list|,
name|maxSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setFloat
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_RATIO_KEY
argument_list|,
literal|1.0F
argument_list|)
expr_stmt|;
block|}
comment|/**    * Setting up a Store    * @throws IOException with error    */
specifier|protected
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|basedir
init|=
operator|new
name|Path
argument_list|(
name|DIR
argument_list|)
decl_stmt|;
name|String
name|logName
init|=
literal|"logs"
decl_stmt|;
name|Path
name|logdir
init|=
operator|new
name|Path
argument_list|(
name|DIR
argument_list|,
name|logName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
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
name|fs
operator|.
name|delete
argument_list|(
name|logdir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
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
decl_stmt|;
name|hlog
operator|=
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|basedir
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|region
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|basedir
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|,
name|hlog
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|basedir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|region
operator|=
operator|new
name|HRegion
argument_list|(
name|tableDir
argument_list|,
name|hlog
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|store
operator|=
operator|new
name|HStore
argument_list|(
name|region
argument_list|,
name|hcd
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|TEST_FILE
operator|=
name|region
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|createTempName
argument_list|()
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|TEST_FILE
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
name|IOException
block|{
name|IOException
name|ex
init|=
literal|null
decl_stmt|;
try|try
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
try|try
block|{
name|hlog
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
if|if
condition|(
name|ex
operator|!=
literal|null
condition|)
block|{
throw|throw
name|ex
throw|;
block|}
block|}
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|toArrayList
parameter_list|(
name|long
modifier|...
name|numbers
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|i
range|:
name|numbers
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|sfCreate
parameter_list|(
name|long
modifier|...
name|sizes
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|ageInDisk
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
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ageInDisk
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
return|return
name|sfCreate
argument_list|(
name|toArrayList
argument_list|(
name|sizes
argument_list|)
argument_list|,
name|ageInDisk
argument_list|)
return|;
block|}
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|sfCreate
parameter_list|(
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|sizes
parameter_list|,
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|ageInDisk
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|sfCreate
argument_list|(
literal|false
argument_list|,
name|sizes
argument_list|,
name|ageInDisk
argument_list|)
return|;
block|}
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|sfCreate
parameter_list|(
name|boolean
name|isReference
parameter_list|,
name|long
modifier|...
name|sizes
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|ageInDisk
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|sizes
operator|.
name|length
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
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ageInDisk
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
return|return
name|sfCreate
argument_list|(
name|isReference
argument_list|,
name|toArrayList
argument_list|(
name|sizes
argument_list|)
argument_list|,
name|ageInDisk
argument_list|)
return|;
block|}
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|sfCreate
parameter_list|(
name|boolean
name|isReference
parameter_list|,
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|sizes
parameter_list|,
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|ageInDisk
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|ret
init|=
name|Lists
operator|.
name|newArrayList
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
name|sizes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
operator|new
name|MockHStoreFile
argument_list|(
name|TEST_UTIL
argument_list|,
name|TEST_FILE
argument_list|,
name|sizes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ageInDisk
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|isReference
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
name|long
index|[]
name|getSizes
parameter_list|(
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|sfList
parameter_list|)
block|{
name|long
index|[]
name|aNums
init|=
operator|new
name|long
index|[
name|sfList
operator|.
name|size
argument_list|()
index|]
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
name|sfList
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|aNums
index|[
name|i
index|]
operator|=
name|sfList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
return|return
name|aNums
return|;
block|}
name|void
name|compactEquals
parameter_list|(
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
parameter_list|,
name|long
modifier|...
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|compactEquals
argument_list|(
name|candidates
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
name|void
name|compactEquals
parameter_list|(
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
parameter_list|,
name|boolean
name|forcemajor
parameter_list|,
name|long
modifier|...
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|compactEquals
argument_list|(
name|candidates
argument_list|,
name|forcemajor
argument_list|,
literal|false
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
name|void
name|compactEquals
parameter_list|(
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
parameter_list|,
name|boolean
name|forcemajor
parameter_list|,
name|boolean
name|isOffPeak
parameter_list|,
name|long
modifier|...
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|store
operator|.
name|forceMajor
operator|=
name|forcemajor
expr_stmt|;
comment|// Test Default compactions
name|CompactionRequestImpl
name|result
init|=
operator|(
operator|(
name|RatioBasedCompactionPolicy
operator|)
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|)
operator|.
name|selectCompaction
argument_list|(
name|candidates
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|,
literal|false
argument_list|,
name|isOffPeak
argument_list|,
name|forcemajor
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|result
operator|.
name|getFiles
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isOffPeak
operator|&&
operator|!
name|forcemajor
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|result
operator|.
name|isOffPeak
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|getSizes
argument_list|(
name|actual
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|store
operator|.
name|forceMajor
operator|=
literal|false
expr_stmt|;
block|}
block|}
end_class

end_unit

