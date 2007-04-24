begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|TreeMap
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
name|HMemcache
operator|.
name|Snapshot
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
name|io
operator|.
name|BytesWritable
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
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_class
specifier|public
class|class
name|TestHMemcache
extends|extends
name|TestCase
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HMemcache
name|hmemcache
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_COUNT
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COLUMNS_COUNT
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_FAMILY
init|=
literal|"column"
decl_stmt|;
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|hmemcache
operator|=
operator|new
name|HMemcache
argument_list|()
expr_stmt|;
comment|// Set up a configuration that has configuration for a file
comment|// filesystem implementation.
name|this
operator|.
name|conf
operator|=
operator|new
name|HBaseConfiguration
argument_list|()
expr_stmt|;
comment|// The test hadoop-site.xml doesn't have a default file fs
comment|// implementation. Remove below when gets added.
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"fs.file.impl"
argument_list|,
literal|"org.apache.hadoop.fs.LocalFileSystem"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Text
name|getRowName
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
block|{
return|return
operator|new
name|Text
argument_list|(
literal|"row"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|index
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Text
name|getColumnName
parameter_list|(
specifier|final
name|int
name|rowIndex
parameter_list|,
specifier|final
name|int
name|colIndex
parameter_list|)
block|{
return|return
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|rowIndex
argument_list|)
operator|+
literal|";"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|colIndex
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Adds {@link #ROW_COUNT} rows and {@link #COLUMNS_COUNT}    * @param hmc Instance to add rows to.    */
specifier|private
name|void
name|addRows
parameter_list|(
specifier|final
name|HMemcache
name|hmc
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columns
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|COLUMNS_COUNT
condition|;
name|ii
operator|++
control|)
block|{
name|Text
name|k
init|=
name|getColumnName
argument_list|(
name|i
argument_list|,
name|ii
argument_list|)
decl_stmt|;
name|columns
operator|.
name|put
argument_list|(
name|k
argument_list|,
name|k
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|hmc
operator|.
name|add
argument_list|(
name|getRowName
argument_list|(
name|i
argument_list|)
argument_list|,
name|columns
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HLog
name|getLogfile
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Create a log file.
name|Path
name|testDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hadoop.tmp.dir"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.tmp.dir"
argument_list|)
argument_list|)
argument_list|,
literal|"hbase"
argument_list|)
decl_stmt|;
name|Path
name|logFile
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
name|this
operator|.
name|getName
argument_list|()
argument_list|)
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
comment|// Cleanup any old log file.
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|logFile
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|logFile
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|logFile
argument_list|,
name|this
operator|.
name|conf
argument_list|)
return|;
block|}
specifier|private
name|Snapshot
name|runSnapshot
parameter_list|(
specifier|final
name|HMemcache
name|hmc
parameter_list|,
specifier|final
name|HLog
name|log
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Save off old state.
name|int
name|oldHistorySize
init|=
name|hmc
operator|.
name|history
operator|.
name|size
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|BytesWritable
argument_list|>
name|oldMemcache
init|=
name|hmc
operator|.
name|memcache
decl_stmt|;
comment|// Run snapshot.
name|Snapshot
name|s
init|=
name|hmc
operator|.
name|snapshotMemcacheForLog
argument_list|(
name|log
argument_list|)
decl_stmt|;
comment|// Make some assertions about what just happened.
name|assertEquals
argument_list|(
literal|"Snapshot equals old memcache"
argument_list|,
name|hmc
operator|.
name|snapshot
argument_list|,
name|oldMemcache
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Returned snapshot holds old memcache"
argument_list|,
name|s
operator|.
name|memcacheSnapshot
argument_list|,
name|oldMemcache
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"History has been incremented"
argument_list|,
name|oldHistorySize
operator|+
literal|1
argument_list|,
name|hmc
operator|.
name|history
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"History holds old snapshot"
argument_list|,
name|hmc
operator|.
name|history
operator|.
name|get
argument_list|(
name|oldHistorySize
argument_list|)
argument_list|,
name|oldMemcache
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
specifier|public
name|void
name|testSnapshotting
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|snapshotCount
init|=
literal|5
decl_stmt|;
specifier|final
name|Text
name|tableName
init|=
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|HLog
name|log
init|=
name|getLogfile
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Add some rows, run a snapshot. Do it a few times.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|snapshotCount
condition|;
name|i
operator|++
control|)
block|{
name|addRows
argument_list|(
name|this
operator|.
name|hmemcache
argument_list|)
expr_stmt|;
name|Snapshot
name|s
init|=
name|runSnapshot
argument_list|(
name|this
operator|.
name|hmemcache
argument_list|,
name|log
argument_list|)
decl_stmt|;
name|log
operator|.
name|completeCacheFlush
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|tableName
argument_list|,
name|s
operator|.
name|sequenceId
argument_list|)
expr_stmt|;
comment|// Clean up snapshot now we are done with it.
name|this
operator|.
name|hmemcache
operator|.
name|deleteSnapshot
argument_list|()
expr_stmt|;
block|}
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|log
operator|.
name|dir
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|conf
argument_list|)
operator|.
name|delete
argument_list|(
name|log
operator|.
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|isExpectedRow
parameter_list|(
specifier|final
name|int
name|rowIndex
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|row
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Text
name|colname
range|:
name|row
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|expectedColname
init|=
name|getColumnName
argument_list|(
name|rowIndex
argument_list|,
name|i
operator|++
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|colnameStr
init|=
name|colname
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Column name"
argument_list|,
name|colnameStr
argument_list|,
name|expectedColname
argument_list|)
expr_stmt|;
comment|// Value is column name as bytes.  Usually result is
comment|// 100 bytes in size at least. This is the default size
comment|// for BytesWriteable.  For comparison, comvert bytes to
comment|// String and trim to remove trailing null bytes.
name|String
name|colvalueStr
init|=
operator|new
name|String
argument_list|(
name|row
operator|.
name|get
argument_list|(
name|colname
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Content"
argument_list|,
name|colnameStr
argument_list|,
name|colvalueStr
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testGetFull
parameter_list|()
throws|throws
name|IOException
block|{
name|addRows
argument_list|(
name|this
operator|.
name|hmemcache
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
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|HStoreKey
name|hsk
init|=
operator|new
name|HStoreKey
argument_list|(
name|getRowName
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|all
init|=
name|this
operator|.
name|hmemcache
operator|.
name|getFull
argument_list|(
name|hsk
argument_list|)
decl_stmt|;
name|isExpectedRow
argument_list|(
name|i
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|addRows
argument_list|(
name|this
operator|.
name|hmemcache
argument_list|)
expr_stmt|;
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Text
index|[]
name|cols
init|=
operator|new
name|Text
index|[
name|COLUMNS_COUNT
operator|*
name|ROW_COUNT
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
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|COLUMNS_COUNT
condition|;
name|ii
operator|++
control|)
block|{
name|cols
index|[
operator|(
name|ii
operator|+
operator|(
name|i
operator|*
name|COLUMNS_COUNT
operator|)
operator|)
index|]
operator|=
name|getColumnName
argument_list|(
name|i
argument_list|,
name|ii
argument_list|)
expr_stmt|;
block|}
block|}
name|HScannerInterface
name|scanner
init|=
name|this
operator|.
name|hmemcache
operator|.
name|getScanner
argument_list|(
name|timestamp
argument_list|,
name|cols
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
literal|"Row name"
argument_list|,
name|key
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|getRowName
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Count of columns"
argument_list|,
name|COLUMNS_COUNT
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|isExpectedRow
argument_list|(
name|i
argument_list|,
name|results
argument_list|)
expr_stmt|;
comment|// Clear out set.  Otherwise row results accumulate.
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

