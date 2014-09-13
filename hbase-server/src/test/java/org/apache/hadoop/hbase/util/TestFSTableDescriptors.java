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
name|util
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|assertNotNull
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
name|assertNull
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
name|assertTrue
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
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|TableDescriptors
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
name|TableExistsException
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
name|testclassification
operator|.
name|MiscTests
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
comment|/**  * Tests for {@link FSTableDescriptors}.  */
end_comment

begin_comment
comment|// Do not support to be executed in he same JVM as other tests
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestFSTableDescriptors
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
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
name|TestFSTableDescriptors
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testRegexAgainstOldStyleTableInfo
parameter_list|()
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|,
name|FSTableDescriptors
operator|.
name|TABLEINFO_FILE_PREFIX
argument_list|)
decl_stmt|;
name|int
name|i
init|=
name|FSTableDescriptors
operator|.
name|getTableInfoSequenceId
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
comment|// Assert it won't eat garbage -- that it fails
name|p
operator|=
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|,
literal|"abc"
argument_list|)
expr_stmt|;
name|FSTableDescriptors
operator|.
name|getTableInfoSequenceId
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateAndUpdate
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|testdir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"testCreateAndUpdate"
argument_list|)
decl_stmt|;
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
literal|"testCreate"
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FSTableDescriptors
name|fstd
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|testdir
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fstd
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fstd
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|statuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|testdir
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"statuses.length="
operator|+
name|statuses
operator|.
name|length
argument_list|,
name|statuses
operator|.
name|length
operator|==
literal|1
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|fstd
operator|.
name|updateTableDescriptor
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
name|statuses
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|testdir
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statuses
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|Path
name|tmpTableDir
init|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|testdir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
literal|".tmp"
argument_list|)
decl_stmt|;
name|statuses
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tmpTableDir
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statuses
operator|.
name|length
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSequenceIdAdvancesOnTableInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|testdir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"testSequenceidAdvancesOnTableInfo"
argument_list|)
decl_stmt|;
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
literal|"testSequenceidAdvancesOnTableInfo"
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FSTableDescriptors
name|fstd
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|testdir
argument_list|)
decl_stmt|;
name|Path
name|p0
init|=
name|fstd
operator|.
name|updateTableDescriptor
argument_list|(
name|htd
argument_list|)
decl_stmt|;
name|int
name|i0
init|=
name|FSTableDescriptors
operator|.
name|getTableInfoSequenceId
argument_list|(
name|p0
argument_list|)
decl_stmt|;
name|Path
name|p1
init|=
name|fstd
operator|.
name|updateTableDescriptor
argument_list|(
name|htd
argument_list|)
decl_stmt|;
comment|// Assert we cleaned up the old file.
name|assertTrue
argument_list|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p0
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|i1
init|=
name|FSTableDescriptors
operator|.
name|getTableInfoSequenceId
argument_list|(
name|p1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|i1
operator|==
name|i0
operator|+
literal|1
argument_list|)
expr_stmt|;
name|Path
name|p2
init|=
name|fstd
operator|.
name|updateTableDescriptor
argument_list|(
name|htd
argument_list|)
decl_stmt|;
comment|// Assert we cleaned up the old file.
name|assertTrue
argument_list|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p1
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|i2
init|=
name|FSTableDescriptors
operator|.
name|getTableInfoSequenceId
argument_list|(
name|p2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|i2
operator|==
name|i1
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFormatTableInfoSequenceId
parameter_list|()
block|{
name|Path
name|p0
init|=
name|assertWriteAndReadSequenceId
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Assert p0 has format we expect.
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
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
name|FSTableDescriptors
operator|.
name|WIDTH_OF_SEQUENCE_ID
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"0"
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|FSTableDescriptors
operator|.
name|TABLEINFO_FILE_PREFIX
operator|+
literal|"."
operator|+
name|sb
operator|.
name|toString
argument_list|()
argument_list|,
name|p0
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check a few more.
name|Path
name|p2
init|=
name|assertWriteAndReadSequenceId
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Path
name|p10000
init|=
name|assertWriteAndReadSequenceId
argument_list|(
literal|10000
argument_list|)
decl_stmt|;
comment|// Get a .tablinfo that has no sequenceid suffix.
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|p0
operator|.
name|getParent
argument_list|()
argument_list|,
name|FSTableDescriptors
operator|.
name|TABLEINFO_FILE_PREFIX
argument_list|)
decl_stmt|;
name|FileStatus
name|fs
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|p
argument_list|)
decl_stmt|;
name|FileStatus
name|fs0
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|p0
argument_list|)
decl_stmt|;
name|FileStatus
name|fs2
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|p2
argument_list|)
decl_stmt|;
name|FileStatus
name|fs10000
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|p10000
argument_list|)
decl_stmt|;
name|Comparator
argument_list|<
name|FileStatus
argument_list|>
name|comparator
init|=
name|FSTableDescriptors
operator|.
name|TABLEINFO_FILESTATUS_COMPARATOR
decl_stmt|;
name|assertTrue
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|fs
argument_list|,
name|fs0
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|fs0
argument_list|,
name|fs2
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|comparator
operator|.
name|compare
argument_list|(
name|fs2
argument_list|,
name|fs10000
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Path
name|assertWriteAndReadSequenceId
parameter_list|(
specifier|final
name|int
name|i
parameter_list|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|,
name|FSTableDescriptors
operator|.
name|getTableInfoFileName
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|ii
init|=
name|FSTableDescriptors
operator|.
name|getTableInfoSequenceId
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|ii
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoves
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"testRemoves"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Cleanup old tests if any detrius laying around.
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|TableDescriptors
name|htds
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
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
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|htds
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|htds
operator|.
name|remove
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|htds
operator|.
name|remove
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadingHTDFromFS
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"testReadingHTDFromFS"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
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
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|rootdir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|FSTableDescriptors
name|fstd
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
name|fstd
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd2
init|=
name|FSTableDescriptors
operator|.
name|getTableDescriptorFromFs
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|htd
operator|.
name|equals
argument_list|(
name|htd2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTableDescriptors
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|name
init|=
literal|"testHTableDescriptors"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Cleanup old tests if any debris laying around.
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|FSTableDescriptors
name|htds
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|get
parameter_list|(
name|TableName
name|tablename
parameter_list|)
throws|throws
name|TableExistsException
throws|,
name|FileNotFoundException
throws|,
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
name|tablename
operator|+
literal|", cachehits="
operator|+
name|this
operator|.
name|cachehits
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|get
argument_list|(
name|tablename
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|final
name|int
name|count
init|=
literal|10
decl_stmt|;
comment|// Write out table infos.
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
operator|+
name|i
argument_list|)
decl_stmt|;
name|htds
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
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
name|assertTrue
argument_list|(
name|htds
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|+
name|i
argument_list|)
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
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
name|assertTrue
argument_list|(
name|htds
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|+
name|i
argument_list|)
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Update the table infos
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
name|name
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|""
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|htds
operator|.
name|updateTableDescriptor
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
comment|// Wait a while so mod time we write is for sure different.
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|htds
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|+
name|i
argument_list|)
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
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
name|assertTrue
argument_list|(
name|htds
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|+
name|i
argument_list|)
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
operator|*
literal|4
argument_list|,
name|htds
operator|.
name|invocations
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected="
operator|+
operator|(
name|count
operator|*
literal|2
operator|)
operator|+
literal|", actual="
operator|+
name|htds
operator|.
name|cachehits
argument_list|,
name|htds
operator|.
name|cachehits
operator|>=
operator|(
name|count
operator|*
literal|2
operator|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoSuchTable
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"testNoSuchTable"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Cleanup old tests if any detrius laying around.
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|TableDescriptors
name|htds
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"There shouldn't be any HTD for this table"
argument_list|,
name|htds
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"NoSuchTable"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdates
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"testUpdates"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Cleanup old tests if any detrius laying around.
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|TableDescriptors
name|htds
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
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
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|htds
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htds
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|htds
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableInfoFileStatusComparator
parameter_list|()
block|{
name|FileStatus
name|bare
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|,
name|FSTableDescriptors
operator|.
name|TABLEINFO_FILE_PREFIX
argument_list|)
argument_list|)
decl_stmt|;
name|FileStatus
name|future
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp/tablinfo."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|FileStatus
name|farFuture
init|=
operator|new
name|FileStatus
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp/tablinfo."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|1000
argument_list|)
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|alist
init|=
block|{
name|bare
block|,
name|future
block|,
name|farFuture
block|}
decl_stmt|;
name|FileStatus
index|[]
name|blist
init|=
block|{
name|bare
block|,
name|farFuture
block|,
name|future
block|}
decl_stmt|;
name|FileStatus
index|[]
name|clist
init|=
block|{
name|farFuture
block|,
name|bare
block|,
name|future
block|}
decl_stmt|;
name|Comparator
argument_list|<
name|FileStatus
argument_list|>
name|c
init|=
name|FSTableDescriptors
operator|.
name|TABLEINFO_FILESTATUS_COMPARATOR
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|alist
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|blist
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|clist
argument_list|,
name|c
argument_list|)
expr_stmt|;
comment|// Now assert all sorted same in way we want.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|alist
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|alist
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|blist
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|blist
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|clist
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|clist
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|i
operator|==
literal|0
condition|?
name|farFuture
else|:
name|i
operator|==
literal|1
condition|?
name|future
else|:
name|bare
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadingArchiveDirectoryFromFS
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be able to read a table descriptor for the archive directory."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Correctly got error when reading a table descriptor from the archive directory: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateTableDescriptorUpdatesIfExistsAlready
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|testdir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"testCreateTableDescriptorUpdatesIfThereExistsAlready"
argument_list|)
decl_stmt|;
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
literal|"testCreateTableDescriptorUpdatesIfThereExistsAlready"
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
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FSTableDescriptors
name|fstd
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|fs
argument_list|,
name|testdir
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fstd
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fstd
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mykey"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myValue"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fstd
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
argument_list|)
expr_stmt|;
comment|//this will re-create
name|Path
name|tableDir
init|=
name|fstd
operator|.
name|getTableDir
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tmpTableDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|FSTableDescriptors
operator|.
name|TMP_DIR
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|statuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tmpTableDir
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statuses
operator|.
name|length
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|htd
argument_list|,
name|FSTableDescriptors
operator|.
name|getTableDescriptorFromFs
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

