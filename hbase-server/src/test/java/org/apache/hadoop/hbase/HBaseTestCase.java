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
name|NavigableMap
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
name|client
operator|.
name|Durability
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
name|Get
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
name|Put
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
name|Table
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
name|Region
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
name|RegionAsTable
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
name|FSTableDescriptors
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
name|hdfs
operator|.
name|MiniDFSCluster
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|AssertionFailedError
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

begin_comment
comment|/**  * Abstract HBase test class.  Initializes a few things that can come in handly  * like an HBaseConfiguration and filesystem.  * @deprecated Write junit4 unit tests using {@link HBaseTestingUtility}  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
specifier|abstract
class|class
name|HBaseTestCase
extends|extends
name|TestCase
block|{
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
name|HBaseTestCase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfamily11"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfamily21"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfamily31"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|COLUMNS
init|=
block|{
name|fam1
block|,
name|fam2
block|,
name|fam3
block|}
decl_stmt|;
specifier|private
name|boolean
name|localfs
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|static
name|Path
name|testDir
init|=
literal|null
decl_stmt|;
specifier|protected
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
specifier|protected
name|HRegion
name|meta
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|char
name|FIRST_CHAR
init|=
literal|'a'
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|char
name|LAST_CHAR
init|=
literal|'z'
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|PUNCTUATION
init|=
literal|"~`@#$%^&*()-_+=:;',.<>/?[]{}|"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|START_KEY_BYTES
init|=
block|{
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|,
name|FIRST_CHAR
block|}
decl_stmt|;
specifier|protected
name|String
name|START_KEY
init|=
operator|new
name|String
argument_list|(
name|START_KEY_BYTES
argument_list|,
name|HConstants
operator|.
name|UTF8_CHARSET
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|MAXVERSIONS
init|=
literal|3
decl_stmt|;
specifier|protected
specifier|final
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|public
specifier|volatile
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|public
specifier|final
name|FSTableDescriptors
name|fsTableDescriptors
decl_stmt|;
block|{
try|try
block|{
name|fsTableDescriptors
operator|=
operator|new
name|FSTableDescriptors
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to init descriptors"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/** constructor */
specifier|public
name|HBaseTestCase
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param name    */
specifier|public
name|HBaseTestCase
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**    * Note that this method must be called after the mini hdfs cluster has    * started or we end up with a local file system.    */
annotation|@
name|Override
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
name|localfs
operator|=
operator|(
name|conf
operator|.
name|get
argument_list|(
literal|"fs.defaultFS"
argument_list|,
literal|"file:///"
argument_list|)
operator|.
name|compareTo
argument_list|(
literal|"file:///"
argument_list|)
operator|==
literal|0
operator|)
expr_stmt|;
if|if
condition|(
name|fs
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|localfs
condition|)
block|{
name|testDir
operator|=
name|getUnitTestdir
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|testDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|testDir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"error during setup"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
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
try|try
block|{
if|if
condition|(
name|localfs
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|testDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"error during tear down"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/**    * @see HBaseTestingUtility#getBaseTestDir    * @param testName    * @return directory to use for this test    */
specifier|protected
name|Path
name|getUnitTestdir
parameter_list|(
name|String
name|testName
parameter_list|)
block|{
return|return
name|testUtil
operator|.
name|getDataTestDir
argument_list|(
name|testName
argument_list|)
return|;
block|}
comment|/**    * You must call close on the returned region and then close on the log file it created. Do    * {@link HBaseTestingUtility#closeRegionAndWAL(HRegion)} to close both the region and the WAL.    * @param desc    * @param startKey    * @param endKey    * @return An {@link HRegion}    * @throws IOException    */
specifier|public
name|HRegion
name|createNewHRegion
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createNewHRegion
argument_list|(
name|desc
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|this
operator|.
name|conf
argument_list|)
return|;
block|}
specifier|public
name|HRegion
name|createNewHRegion
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
decl_stmt|;
return|return
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|hri
argument_list|,
name|testDir
argument_list|,
name|conf
argument_list|,
name|desc
argument_list|)
return|;
block|}
specifier|protected
name|HRegion
name|openClosedRegion
parameter_list|(
specifier|final
name|HRegion
name|closedRegion
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|closedRegion
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Create a table of name<code>name</code> with {@link COLUMNS} for    * families.    * @param name Name to give table.    * @return Column descriptor.    */
specifier|protected
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
name|createTableDescriptor
argument_list|(
name|name
argument_list|,
name|MAXVERSIONS
argument_list|)
return|;
block|}
comment|/**    * Create a table of name<code>name</code> with {@link COLUMNS} for    * families.    * @param name Name to give table.    * @param versions How many versions to allow per column.    * @return Column descriptor.    */
specifier|protected
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|versions
parameter_list|)
block|{
return|return
name|createTableDescriptor
argument_list|(
name|name
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_MIN_VERSIONS
argument_list|,
name|versions
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_KEEP_DELETED
argument_list|)
return|;
block|}
comment|/**    * Create a table of name<code>name</code> with {@link COLUMNS} for    * families.    * @param name Name to give table.    * @param versions How many versions to allow per column.    * @return Column descriptor.    */
specifier|protected
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|minVersions
parameter_list|,
specifier|final
name|int
name|versions
parameter_list|,
specifier|final
name|int
name|ttl
parameter_list|,
name|KeepDeletedCells
name|keepDeleted
parameter_list|)
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
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|cfName
range|:
operator|new
name|byte
index|[]
index|[]
block|{
name|fam1
block|,
name|fam2
block|,
name|fam3
block|}
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|cfName
argument_list|)
operator|.
name|setMinVersions
argument_list|(
name|minVersions
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
operator|.
name|setKeepDeletedCells
argument_list|(
name|keepDeleted
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|htd
return|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @param r    * @param columnFamily    * @param column    * @throws IOException    * @return count of what we added.    */
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Region
name|r
parameter_list|,
specifier|final
name|byte
index|[]
name|columnFamily
parameter_list|,
specifier|final
name|byte
index|[]
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|startKey
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEndKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|startKeyBytes
init|=
name|startKey
decl_stmt|;
if|if
condition|(
name|startKeyBytes
operator|==
literal|null
operator|||
name|startKeyBytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|startKeyBytes
operator|=
name|START_KEY_BYTES
expr_stmt|;
block|}
return|return
name|addContent
argument_list|(
operator|new
name|RegionAsTable
argument_list|(
name|r
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|columnFamily
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|column
argument_list|)
argument_list|,
name|startKeyBytes
argument_list|,
name|endKey
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Region
name|r
parameter_list|,
specifier|final
name|byte
index|[]
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|addContent
argument_list|(
name|r
argument_list|,
name|columnFamily
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @throws IOException    * @return count of what we added.    */
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Table
name|updater
parameter_list|,
specifier|final
name|String
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|addContent
argument_list|(
name|updater
argument_list|,
name|columnFamily
argument_list|,
name|START_KEY_BYTES
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Table
name|updater
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|addContent
argument_list|(
name|updater
argument_list|,
name|family
argument_list|,
name|column
argument_list|,
name|START_KEY_BYTES
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @return count of what we added.    * @throws IOException    */
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Table
name|updater
parameter_list|,
specifier|final
name|String
name|columnFamily
parameter_list|,
specifier|final
name|byte
index|[]
name|startKeyBytes
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|addContent
argument_list|(
name|updater
argument_list|,
name|columnFamily
argument_list|,
literal|null
argument_list|,
name|startKeyBytes
argument_list|,
name|endKey
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Table
name|updater
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
name|String
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|startKeyBytes
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|addContent
argument_list|(
name|updater
argument_list|,
name|family
argument_list|,
name|column
argument_list|,
name|startKeyBytes
argument_list|,
name|endKey
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @return count of what we added.    * @throws IOException    */
specifier|public
specifier|static
name|long
name|addContent
parameter_list|(
specifier|final
name|Table
name|updater
parameter_list|,
specifier|final
name|String
name|columnFamily
parameter_list|,
specifier|final
name|String
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|startKeyBytes
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|count
init|=
literal|0
decl_stmt|;
comment|// Add rows of three characters.  The first character starts with the
comment|// 'a' character and runs up to 'z'.  Per first character, we run the
comment|// second character over same range.  And same for the third so rows
comment|// (and values) look like this: 'aaa', 'aab', 'aac', etc.
name|char
name|secondCharStart
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|1
index|]
decl_stmt|;
name|char
name|thirdCharStart
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|2
index|]
decl_stmt|;
name|EXIT
label|:
for|for
control|(
name|char
name|c
init|=
operator|(
name|char
operator|)
name|startKeyBytes
index|[
literal|0
index|]
init|;
name|c
operator|<=
name|LAST_CHAR
condition|;
name|c
operator|++
control|)
block|{
for|for
control|(
name|char
name|d
init|=
name|secondCharStart
init|;
name|d
operator|<=
name|LAST_CHAR
condition|;
name|d
operator|++
control|)
block|{
for|for
control|(
name|char
name|e
init|=
name|thirdCharStart
init|;
name|e
operator|<=
name|LAST_CHAR
condition|;
name|e
operator|++
control|)
block|{
name|byte
index|[]
name|t
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|c
block|,
operator|(
name|byte
operator|)
name|d
block|,
operator|(
name|byte
operator|)
name|e
block|}
decl_stmt|;
if|if
condition|(
name|endKey
operator|!=
literal|null
operator|&&
name|endKey
operator|.
name|length
operator|>
literal|0
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|endKey
argument_list|,
name|t
argument_list|)
operator|<=
literal|0
condition|)
block|{
break|break
name|EXIT
break|;
block|}
try|try
block|{
name|Put
name|put
decl_stmt|;
if|if
condition|(
name|ts
operator|!=
operator|-
literal|1
condition|)
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|t
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|column
operator|!=
literal|null
operator|&&
name|column
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|columnFamily
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|columnFamily
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|columnFamily
operator|.
name|endsWith
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|column
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|byte
index|[]
index|[]
name|split
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|put
operator|.
name|add
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|split
index|[
literal|1
index|]
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
name|updater
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
comment|// Set start character back to FIRST_CHAR after we've done first loop.
name|thirdCharStart
operator|=
name|FIRST_CHAR
expr_stmt|;
block|}
name|secondCharStart
operator|=
name|FIRST_CHAR
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|protected
name|void
name|assertResultEquals
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|setTimeStamp
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
name|Result
name|res
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|map
init|=
name|res
operator|.
name|getMap
argument_list|()
decl_stmt|;
name|byte
index|[]
name|res_value
init|=
name|map
operator|.
name|get
argument_list|(
name|family
argument_list|)
operator|.
name|get
argument_list|(
name|qualifier
argument_list|)
operator|.
name|get
argument_list|(
name|timestamp
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
operator|+
literal|" at timestamp "
operator|+
name|timestamp
argument_list|,
literal|null
argument_list|,
name|res_value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|res_value
operator|==
literal|null
condition|)
block|{
name|fail
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
operator|+
literal|" at timestamp "
operator|+
name|timestamp
operator|+
literal|"\" was expected to be \""
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
argument_list|)
operator|+
literal|" but was null"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|res_value
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
operator|+
literal|" at timestamp "
operator|+
name|timestamp
argument_list|,
name|value
argument_list|,
operator|new
name|String
argument_list|(
name|res_value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Common method to close down a MiniDFSCluster and the associated file system    *    * @param cluster    */
specifier|public
specifier|static
name|void
name|shutdownDfs
parameter_list|(
name|MiniDFSCluster
name|cluster
parameter_list|)
block|{
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down Mini DFS "
argument_list|)
expr_stmt|;
try|try
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|/// Can get a java.lang.reflect.UndeclaredThrowableException thrown
comment|// here because of an InterruptedException. Don't let exceptions in
comment|// here be cause of test failure.
block|}
try|try
block|{
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down FileSystem"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|FileSystem
operator|.
name|closeAll
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
name|error
argument_list|(
literal|"error closing file system"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * You must call {@link #closeRootAndMeta()} when done after calling this    * method. It does cleanup.    * @throws IOException    */
specifier|protected
name|void
name|createMetaRegion
parameter_list|()
throws|throws
name|IOException
block|{
name|FSTableDescriptors
name|fsTableDescriptors
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|meta
operator|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|testDir
argument_list|,
name|conf
argument_list|,
name|fsTableDescriptors
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|closeRootAndMeta
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|meta
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertByteEquals
parameter_list|(
name|byte
index|[]
name|expected
parameter_list|,
name|byte
index|[]
name|actual
parameter_list|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionFailedError
argument_list|(
literal|"expected:<"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
operator|+
literal|"> but was:<"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|actual
argument_list|)
operator|+
literal|">"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|assertEquals
parameter_list|(
name|byte
index|[]
name|expected
parameter_list|,
name|byte
index|[]
name|actual
parameter_list|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionFailedError
argument_list|(
literal|"expected:<"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|expected
argument_list|)
operator|+
literal|"> but was:<"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|actual
argument_list|)
operator|+
literal|">"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

