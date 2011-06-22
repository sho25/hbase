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
name|*
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
name|Test
import|;
end_import

begin_comment
comment|/**  * Tests for {@link FSTableDescriptors}.  */
end_comment

begin_class
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
name|HBaseTestingUtility
operator|.
name|getTestDir
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
name|name
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
name|getNameAsString
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
name|getNameAsString
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
name|name
argument_list|)
decl_stmt|;
name|Path
name|rootdir
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|createHTDInFS
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd2
init|=
name|FSUtils
operator|.
name|getTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
operator|.
name|getNameAsString
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
specifier|private
name|void
name|createHTDInFS
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|FSUtils
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
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
comment|// Cleanup old tests if any detrius laying around.
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|name
argument_list|)
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
name|createHTDInFS
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
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
name|byte
index|[]
name|tablename
parameter_list|)
throws|throws
name|TableExistsException
throws|,
name|FileNotFoundException
throws|,
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tablename
argument_list|)
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
name|Bytes
operator|.
name|toBytes
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
name|Bytes
operator|.
name|toBytes
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
name|name
operator|+
name|i
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
name|FSUtils
operator|.
name|updateHTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
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
name|Bytes
operator|.
name|toBytes
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
name|Bytes
operator|.
name|toBytes
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
name|assertEquals
argument_list|(
name|count
operator|*
literal|2
argument_list|,
name|htds
operator|.
name|cachehits
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|htds
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|htds
operator|.
name|invocations
argument_list|,
name|count
operator|*
literal|4
operator|+
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|htds
operator|.
name|cachehits
argument_list|,
name|count
operator|*
literal|2
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
operator|.
name|class
argument_list|)
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
name|HBaseTestingUtility
operator|.
name|getTestDir
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
name|htds
operator|.
name|get
argument_list|(
literal|"NoSuchTable"
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
name|HBaseTestingUtility
operator|.
name|getTestDir
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
name|name
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
block|}
end_class

end_unit

