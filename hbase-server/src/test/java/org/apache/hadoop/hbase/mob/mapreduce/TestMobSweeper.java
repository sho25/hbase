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
name|mob
operator|.
name|mapreduce
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|client
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|TestMobSweeper
block|{
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
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|row
init|=
literal|"row_"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|family
init|=
literal|"family"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|column
init|=
literal|"column"
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
name|BufferedMutator
name|bufMut
decl_stmt|;
specifier|private
specifier|static
name|Admin
name|admin
decl_stmt|;
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
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
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.info.port.auto"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|15
argument_list|)
expr_stmt|;
comment|// avoid major compactions
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.max"
argument_list|,
literal|30
argument_list|)
expr_stmt|;
comment|// avoid major compactions
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Before
specifier|public
name|void
name|setUp
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
name|tableName
operator|=
literal|"testSweeper"
operator|+
name|tid
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|3L
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Connection
name|c
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|=
name|c
operator|.
name|getTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|bufMut
operator|=
name|c
operator|.
name|getBufferedMutator
argument_list|(
name|tn
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
name|admin
operator|.
name|disableTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Path
name|getMobFamilyPath
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tableNameStr
parameter_list|,
name|String
name|familyName
parameter_list|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|MobUtils
operator|.
name|getMobRegionPath
argument_list|(
name|conf
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableNameStr
argument_list|)
argument_list|)
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
return|return
name|p
return|;
block|}
specifier|private
name|String
name|mergeString
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|set
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|set
control|)
name|sb
operator|.
name|append
argument_list|(
name|s
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|generateMobTable
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|BufferedMutator
name|table
parameter_list|,
name|String
name|tableName
parameter_list|,
name|int
name|count
parameter_list|,
name|int
name|flushStep
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|count
operator|<=
literal|0
operator|||
name|flushStep
operator|<=
literal|0
condition|)
return|return;
name|int
name|index
init|=
literal|0
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|mobVal
init|=
operator|new
name|byte
index|[
literal|101
operator|*
literal|1024
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|mobVal
argument_list|)
expr_stmt|;
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
name|row
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
name|mobVal
argument_list|)
expr_stmt|;
name|table
operator|.
name|mutate
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|index
operator|++
operator|%
name|flushStep
operator|==
literal|0
condition|)
block|{
name|table
operator|.
name|flush
argument_list|()
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|table
operator|.
name|flush
argument_list|()
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSweeper
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|10
decl_stmt|;
comment|//create table and generate 10 mob files
name|generateMobTable
argument_list|(
name|admin
argument_list|,
name|bufMut
argument_list|,
name|tableName
argument_list|,
name|count
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|//get mob files
name|Path
name|mobFamilyPath
init|=
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|fileStatuses
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
decl_stmt|;
comment|// mobFileSet0 stores the original mob files
name|TreeSet
argument_list|<
name|String
argument_list|>
name|mobFilesSet
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fileStatuses
control|)
block|{
name|mobFilesSet
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//scan the table, retreive the references
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_RAW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_REF_ONLY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|rs
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|TreeSet
argument_list|<
name|String
argument_list|>
name|mobFilesScanned
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|rs
control|)
block|{
name|byte
index|[]
name|valueBytes
init|=
name|res
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
name|mobFilesScanned
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|valueBytes
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|,
name|valueBytes
operator|.
name|length
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//there should be 10 mob files
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|mobFilesScanned
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|//check if we store the correct reference of mob files
name|assertEquals
argument_list|(
name|mergeString
argument_list|(
name|mobFilesSet
argument_list|)
argument_list|,
name|mergeString
argument_list|(
name|mobFilesScanned
argument_list|)
argument_list|)
expr_stmt|;
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
name|setLong
argument_list|(
name|SweepJob
operator|.
name|MOB_SWEEP_JOB_DELAY
argument_list|,
literal|24
operator|*
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|args
index|[
literal|0
index|]
operator|=
name|tableName
expr_stmt|;
name|args
index|[
literal|1
index|]
operator|=
name|family
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|Sweeper
argument_list|()
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|mobFamilyPath
operator|=
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|fileStatuses
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
expr_stmt|;
name|mobFilesSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fileStatuses
control|)
block|{
name|mobFilesSet
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|mobFilesSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_RAW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_REF_ONLY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|rs
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|TreeSet
argument_list|<
name|String
argument_list|>
name|mobFilesScannedAfterJob
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|rs
control|)
block|{
name|byte
index|[]
name|valueBytes
init|=
name|res
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
name|mobFilesScannedAfterJob
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|valueBytes
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|,
name|valueBytes
operator|.
name|length
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|mobFilesScannedAfterJob
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fileStatuses
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
expr_stmt|;
name|mobFilesSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fileStatuses
control|)
block|{
name|mobFilesSet
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|mobFilesSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|mobFilesScannedAfterJob
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|mobFilesSet
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testCompactionDelaySweeperInternal
parameter_list|(
name|Table
name|table
parameter_list|,
name|BufferedMutator
name|bufMut
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|10
decl_stmt|;
comment|//create table and generate 10 mob files
name|generateMobTable
argument_list|(
name|admin
argument_list|,
name|bufMut
argument_list|,
name|tableName
argument_list|,
name|count
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|//get mob files
name|Path
name|mobFamilyPath
init|=
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|fileStatuses
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
decl_stmt|;
comment|// mobFileSet0 stores the orignal mob files
name|TreeSet
argument_list|<
name|String
argument_list|>
name|mobFilesSet
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fileStatuses
control|)
block|{
name|mobFilesSet
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//scan the table, retreive the references
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_RAW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_REF_ONLY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|rs
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|TreeSet
argument_list|<
name|String
argument_list|>
name|mobFilesScanned
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|rs
control|)
block|{
name|byte
index|[]
name|valueBytes
init|=
name|res
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
name|mobFilesScanned
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|valueBytes
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|,
name|valueBytes
operator|.
name|length
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//there should be 10 mob files
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|mobFilesScanned
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|//check if we store the correct reference of mob files
name|assertEquals
argument_list|(
name|mergeString
argument_list|(
name|mobFilesSet
argument_list|)
argument_list|,
name|mergeString
argument_list|(
name|mobFilesScanned
argument_list|)
argument_list|)
expr_stmt|;
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
name|setLong
argument_list|(
name|SweepJob
operator|.
name|MOB_SWEEP_JOB_DELAY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|args
index|[
literal|0
index|]
operator|=
name|tableName
expr_stmt|;
name|args
index|[
literal|1
index|]
operator|=
name|family
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|Sweeper
argument_list|()
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|mobFamilyPath
operator|=
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
name|fileStatuses
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
expr_stmt|;
name|mobFilesSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fileStatuses
control|)
block|{
name|mobFilesSet
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mobFilesSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_RAW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_REF_ONLY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|rs
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|TreeSet
argument_list|<
name|String
argument_list|>
name|mobFilesScannedAfterJob
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|rs
control|)
block|{
name|byte
index|[]
name|valueBytes
init|=
name|res
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
name|mobFilesScannedAfterJob
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|valueBytes
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|,
name|valueBytes
operator|.
name|length
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mobFilesScannedAfterJob
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fileStatuses
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
expr_stmt|;
name|mobFilesSet
operator|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|fileStatuses
control|)
block|{
name|mobFilesSet
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mobFilesSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|mobFilesScannedAfterJob
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|mobFilesSet
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionDelaySweeper
parameter_list|()
throws|throws
name|Exception
block|{
name|testCompactionDelaySweeperInternal
argument_list|(
name|table
argument_list|,
name|bufMut
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionDelaySweeperWithNamespace
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create a table with namespace
name|NamespaceDescriptor
name|namespaceDescriptor
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
literal|"ns"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|namespaceDescriptor
argument_list|)
expr_stmt|;
name|String
name|tableNameAsString
init|=
literal|"ns:testSweeperWithNamespace"
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableNameAsString
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|3L
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Connection
name|c
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|BufferedMutator
name|bufMut
init|=
name|c
operator|.
name|getBufferedMutator
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|c
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|testCompactionDelaySweeperInternal
argument_list|(
name|table
argument_list|,
name|bufMut
argument_list|,
name|tableNameAsString
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
literal|"ns"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

