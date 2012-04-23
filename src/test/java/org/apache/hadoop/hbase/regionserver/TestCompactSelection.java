begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|GregorianCalendar
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
name|NoOpDataBlockEncoder
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
name|CompactSelection
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
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
name|TestCompactSelection
extends|extends
name|TestCase
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
name|TestCompactSelection
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Store
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
literal|"TestCompactSelection"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Path
name|TEST_FILE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|minFiles
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|maxFiles
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|minSize
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|maxSize
init|=
literal|1000
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
comment|// setup config values necessary for store
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
literal|"hbase.hstore.compaction.min"
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
literal|"hbase.hstore.compaction.max"
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
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
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
literal|"hbase.hstore.compaction.max.size"
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
literal|"hbase.hstore.compaction.ratio"
argument_list|,
literal|1.0F
argument_list|)
expr_stmt|;
comment|//Setting up a Store
name|Path
name|basedir
init|=
operator|new
name|Path
argument_list|(
name|DIR
argument_list|)
decl_stmt|;
name|Path
name|logdir
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
literal|"/logs"
argument_list|)
decl_stmt|;
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
name|basedir
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
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
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
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
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HLog
name|hlog
init|=
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|logdir
argument_list|,
name|oldLogDir
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
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
argument_list|)
decl_stmt|;
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|Path
name|tableDir
init|=
operator|new
name|Path
argument_list|(
name|basedir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|)
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
name|Store
argument_list|(
name|basedir
argument_list|,
name|region
argument_list|,
name|hcd
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|TEST_FILE
operator|=
name|StoreFile
operator|.
name|getRandomFilename
argument_list|(
name|fs
argument_list|,
name|store
operator|.
name|getHomedir
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|create
argument_list|(
name|TEST_FILE
argument_list|)
expr_stmt|;
block|}
comment|// used so our tests don't deal with actual StoreFiles
specifier|static
class|class
name|MockStoreFile
extends|extends
name|StoreFile
block|{
name|long
name|length
init|=
literal|0
decl_stmt|;
name|boolean
name|isRef
init|=
literal|false
decl_stmt|;
name|MockStoreFile
parameter_list|(
name|long
name|length
parameter_list|,
name|boolean
name|isRef
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|TEST_FILE
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
name|NoOpDataBlockEncoder
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|isRef
operator|=
name|isRef
expr_stmt|;
block|}
name|void
name|setLength
parameter_list|(
name|long
name|newLen
parameter_list|)
block|{
name|this
operator|.
name|length
operator|=
name|newLen
expr_stmt|;
block|}
annotation|@
name|Override
name|boolean
name|isMajorCompaction
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
name|boolean
name|isReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|isRef
return|;
block|}
annotation|@
name|Override
specifier|public
name|StoreFile
operator|.
name|Reader
name|getReader
parameter_list|()
block|{
specifier|final
name|long
name|len
init|=
name|this
operator|.
name|length
decl_stmt|;
return|return
operator|new
name|StoreFile
operator|.
name|Reader
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|length
parameter_list|()
block|{
return|return
name|len
return|;
block|}
block|}
return|;
block|}
block|}
name|List
argument_list|<
name|StoreFile
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
return|return
name|sfCreate
argument_list|(
literal|false
argument_list|,
name|sizes
argument_list|)
return|;
block|}
name|List
argument_list|<
name|StoreFile
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
name|List
argument_list|<
name|StoreFile
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
name|long
name|i
range|:
name|sizes
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
operator|new
name|MockStoreFile
argument_list|(
name|i
argument_list|,
name|isReference
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
name|StoreFile
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
name|StoreFile
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
name|expected
argument_list|)
expr_stmt|;
block|}
name|void
name|compactEquals
parameter_list|(
name|List
argument_list|<
name|StoreFile
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
name|store
operator|.
name|forceMajor
operator|=
name|forcemajor
expr_stmt|;
name|List
argument_list|<
name|StoreFile
argument_list|>
name|actual
init|=
name|store
operator|.
name|compactSelection
argument_list|(
name|candidates
argument_list|)
operator|.
name|getFilesToCompact
argument_list|()
decl_stmt|;
name|store
operator|.
name|forceMajor
operator|=
literal|false
expr_stmt|;
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
block|}
specifier|public
name|void
name|testCompactionRatio
parameter_list|()
throws|throws
name|IOException
block|{
comment|/*      * NOTE: these tests are specific to describe the implementation of the      * current compaction algorithm.  Developed to ensure that refactoring      * doesn't implicitly alter this.      */
name|long
name|tooBig
init|=
name|maxSize
operator|+
literal|1
decl_stmt|;
comment|// default case. preserve user ratio on size
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// less than compact threshold = don't compact
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// greater than compact size = skip those
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
name|tooBig
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|)
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|)
expr_stmt|;
comment|// big size + threshold
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
name|tooBig
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// small files = don't care about ratio
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|8
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|8
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|/* TODO: add sorting + unit test back in when HBASE-2856 is fixed      // sort first so you don't include huge file the tail end     // happens with HFileOutputFormat bulk migration     compactEquals(sfCreate(100,50,23,12,12, 500), 23, 12, 12);      */
comment|// don't exceed max file compact threshold
name|assertEquals
argument_list|(
name|maxFiles
argument_list|,
name|store
operator|.
name|compactSelection
argument_list|(
name|sfCreate
argument_list|(
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|getFilesToCompact
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// note:  file selection starts with largest to smallest.
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|/* MAJOR COMPACTION */
comment|// if a major compaction has been forced, then compact everything
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// also choose files< threshold on major compaction
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// even if one of those files is too big
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// don't exceed max file compact threshold, even with major compaction
name|store
operator|.
name|forceMajor
operator|=
literal|true
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|store
operator|.
name|forceMajor
operator|=
literal|false
expr_stmt|;
comment|// if we exceed maxCompactSize, downgrade to minor
comment|// if not, it creates a 'snowball effect' when files>> maxCompactSize:
comment|// the last file in compaction is the aggregate of all previous compactions
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hregion.majorcompaction.jitter"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
try|try
block|{
comment|// trigger an aged major compaction
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// major sure exceeding maxCompactSize also downgrades aged minors
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|1000
operator|*
literal|60
operator|*
literal|60
operator|*
literal|24
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hregion.majorcompaction.jitter"
argument_list|,
literal|0.20F
argument_list|)
expr_stmt|;
block|}
comment|/* REFERENCES == file is from a region that was split */
comment|// treat storefiles that have references like a major compaction
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
literal|100
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|100
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// reference files shouldn't obey max threshold
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// reference files should obey max file compact to avoid OOM
name|assertEquals
argument_list|(
name|maxFiles
argument_list|,
name|store
operator|.
name|compactSelection
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|getFilesToCompact
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// reference compaction
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// empty case
name|compactEquals
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// empty case (because all files are too big)
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
name|tooBig
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testOffPeakCompactionRatio
parameter_list|()
throws|throws
name|IOException
block|{
comment|/*      * NOTE: these tests are specific to describe the implementation of the      * current compaction algorithm.  Developed to ensure that refactoring      * doesn't implicitly alter this.      */
name|long
name|tooBig
init|=
name|maxSize
operator|+
literal|1
decl_stmt|;
name|Calendar
name|calendar
init|=
operator|new
name|GregorianCalendar
argument_list|()
decl_stmt|;
name|int
name|hourOfDay
init|=
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Hour of day = "
operator|+
name|hourOfDay
argument_list|)
expr_stmt|;
name|int
name|hourPlusOne
init|=
operator|(
operator|(
name|hourOfDay
operator|+
literal|1
operator|+
literal|24
operator|)
operator|%
literal|24
operator|)
decl_stmt|;
name|int
name|hourMinusOne
init|=
operator|(
operator|(
name|hourOfDay
operator|-
literal|1
operator|+
literal|24
operator|)
operator|%
literal|24
operator|)
decl_stmt|;
name|int
name|hourMinusTwo
init|=
operator|(
operator|(
name|hourOfDay
operator|-
literal|2
operator|+
literal|24
operator|)
operator|%
literal|24
operator|)
decl_stmt|;
comment|// check compact selection without peak hour setting
name|LOG
operator|.
name|debug
argument_list|(
literal|"Testing compact selection without off-peak settings..."
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|999
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// set an off-peak compaction threshold
name|this
operator|.
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hstore.compaction.ratio.offpeak"
argument_list|,
literal|5.0F
argument_list|)
expr_stmt|;
comment|// set peak hour to current time and check compact selection
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.offpeak.start.hour"
argument_list|,
name|hourMinusOne
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.offpeak.end.hour"
argument_list|,
name|hourPlusOne
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Testing compact selection with off-peak settings ("
operator|+
name|hourMinusOne
operator|+
literal|", "
operator|+
name|hourPlusOne
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|999
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// set peak hour outside current selection and check compact selection
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.offpeak.start.hour"
argument_list|,
name|hourMinusTwo
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.offpeak.end.hour"
argument_list|,
name|hourMinusOne
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Testing compact selection with off-peak settings ("
operator|+
name|hourMinusTwo
operator|+
literal|", "
operator|+
name|hourMinusOne
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|999
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

