begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|UnsupportedEncodingException
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
name|HColumnDescriptor
operator|.
name|CompressionType
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

begin_comment
comment|/**  * Abstract base class for test cases. Performs all static initialization  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HBaseTestCase
extends|extends
name|TestCase
block|{
specifier|protected
specifier|final
specifier|static
name|String
name|COLFAMILY_NAME1
init|=
literal|"colfamily1:"
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|String
name|COLFAMILY_NAME2
init|=
literal|"colfamily2:"
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|String
name|COLFAMILY_NAME3
init|=
literal|"colfamily3:"
decl_stmt|;
specifier|protected
specifier|static
name|Text
index|[]
name|COLUMNS
init|=
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME1
argument_list|)
block|,
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME2
argument_list|)
block|,
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME3
argument_list|)
block|}
decl_stmt|;
specifier|protected
name|Path
name|testDir
init|=
literal|null
decl_stmt|;
specifier|protected
name|FileSystem
name|localFs
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
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|MAXVERSIONS
init|=
literal|3
decl_stmt|;
static|static
block|{
name|StaticTestEnvironment
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|volatile
name|HBaseConfiguration
name|conf
decl_stmt|;
comment|/**    * constructor    */
specifier|public
name|HBaseTestCase
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|conf
operator|=
operator|new
name|HBaseConfiguration
argument_list|()
expr_stmt|;
try|try
block|{
name|START_KEY
operator|=
operator|new
name|String
argument_list|(
name|START_KEY_BYTES
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
operator|+
name|PUNCTUATION
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
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
name|conf
operator|=
operator|new
name|HBaseConfiguration
argument_list|()
expr_stmt|;
try|try
block|{
name|START_KEY
operator|=
operator|new
name|String
argument_list|(
name|START_KEY_BYTES
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
operator|+
name|PUNCTUATION
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** {@inheritDoc} */
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
name|this
operator|.
name|testDir
operator|=
name|getUnitTestdir
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|localFs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|localFs
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|localFs
operator|.
name|delete
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|localFs
operator|!=
literal|null
operator|&&
name|this
operator|.
name|testDir
operator|!=
literal|null
operator|&&
name|this
operator|.
name|localFs
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|localFs
operator|.
name|delete
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|Path
name|getUnitTestdir
parameter_list|(
name|String
name|testName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|StaticTestEnvironment
operator|.
name|TEST_DIRECTORY_KEY
argument_list|,
name|testName
argument_list|)
return|;
block|}
specifier|protected
name|HRegion
name|createNewHRegion
parameter_list|(
name|Path
name|dir
parameter_list|,
name|Configuration
name|c
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|Text
name|startKey
parameter_list|,
name|Text
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createNewHRegion
argument_list|(
name|dir
argument_list|,
name|c
argument_list|,
operator|new
name|HRegionInfo
argument_list|(
name|desc
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|HRegion
name|createNewHRegion
parameter_list|(
name|Path
name|dir
parameter_list|,
name|Configuration
name|c
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|dir
argument_list|,
name|HRegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dir
operator|.
name|getFileSystem
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|regionDir
argument_list|)
expr_stmt|;
return|return
operator|new
name|HRegion
argument_list|(
name|dir
argument_list|,
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
argument_list|,
name|conf
argument_list|)
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
literal|null
argument_list|)
return|;
block|}
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
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME1
argument_list|)
argument_list|,
name|versions
argument_list|,
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME2
argument_list|)
argument_list|,
name|versions
argument_list|,
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME3
argument_list|)
argument_list|,
name|versions
argument_list|,
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @param r    * @param column    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|addContent
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
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
name|Text
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
operator|.
name|getBytes
argument_list|()
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
name|addContent
argument_list|(
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
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
expr_stmt|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @param updater  An instance of {@link Incommon}.    * @param column    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|addContent
parameter_list|(
specifier|final
name|Incommon
name|updater
parameter_list|,
specifier|final
name|String
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|addContent
argument_list|(
name|updater
argument_list|,
name|column
argument_list|,
name|START_KEY_BYTES
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @param updater  An instance of {@link Incommon}.    * @param column    * @param startKeyBytes Where to start the rows inserted    * @param endKey Where to stop inserting rows.    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|addContent
parameter_list|(
specifier|final
name|Incommon
name|updater
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
name|Text
name|endKey
parameter_list|)
throws|throws
name|IOException
block|{
name|addContent
argument_list|(
name|updater
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
expr_stmt|;
block|}
comment|/**    * Add content to region<code>r</code> on the passed column    *<code>column</code>.    * Adds data of the from 'aaa', 'aab', etc where key and value are the same.    * @param updater  An instance of {@link Incommon}.    * @param column    * @param startKeyBytes Where to start the rows inserted    * @param endKey Where to stop inserting rows.    * @param ts Timestamp to write the content with.    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|addContent
parameter_list|(
specifier|final
name|Incommon
name|updater
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
name|Text
name|endKey
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
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
name|bytes
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
name|String
name|s
init|=
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
operator|+
name|PUNCTUATION
decl_stmt|;
name|bytes
operator|=
name|s
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|endKey
operator|!=
literal|null
operator|&&
name|endKey
operator|.
name|getLength
argument_list|()
operator|>
literal|0
operator|&&
name|endKey
operator|.
name|compareTo
argument_list|(
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
name|long
name|lockid
init|=
name|updater
operator|.
name|startBatchUpdate
argument_list|(
name|t
argument_list|)
decl_stmt|;
try|try
block|{
name|updater
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
name|column
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
if|if
condition|(
name|ts
operator|==
operator|-
literal|1
condition|)
block|{
name|updater
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updater
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
name|lockid
operator|=
operator|-
literal|1
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|lockid
operator|!=
operator|-
literal|1
condition|)
block|{
name|updater
operator|.
name|abort
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
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
block|}
comment|/**    * Implementors can flushcache.    */
specifier|public
specifier|static
interface|interface
name|FlushCache
block|{
specifier|public
name|void
name|flushcache
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Interface used by tests so can do common operations against an HTable    * or an HRegion.    *     * TOOD: Come up w/ a better name for this interface.    */
specifier|public
specifier|static
interface|interface
name|Incommon
block|{
specifier|public
name|byte
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|ts
parameter_list|,
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|long
name|startBatchUpdate
parameter_list|(
specifier|final
name|Text
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|put
parameter_list|(
name|long
name|lockid
parameter_list|,
name|Text
name|column
parameter_list|,
name|byte
name|val
index|[]
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|delete
parameter_list|(
name|long
name|lockid
parameter_list|,
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|deleteAll
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|commit
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|commit
parameter_list|(
name|long
name|lockid
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|abort
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|HScannerInterface
name|getScanner
parameter_list|(
name|Text
index|[]
name|columns
parameter_list|,
name|Text
name|firstRow
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * A class that makes a {@link Incommon} out of a {@link HRegion}    */
specifier|public
specifier|static
class|class
name|HRegionIncommon
implements|implements
name|Incommon
block|{
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|public
name|HRegionIncommon
parameter_list|(
specifier|final
name|HRegion
name|HRegion
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|HRegion
expr_stmt|;
block|}
specifier|public
name|void
name|abort
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|abort
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|commit
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|commit
parameter_list|(
name|long
name|lockid
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|long
name|lockid
parameter_list|,
name|Text
name|column
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|column
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|(
name|long
name|lockid
parameter_list|,
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|delete
argument_list|(
name|lockid
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deleteAll
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|region
operator|.
name|deleteAll
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|startBatchUpdate
parameter_list|(
name|Text
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|region
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
return|;
block|}
specifier|public
name|HScannerInterface
name|getScanner
parameter_list|(
name|Text
index|[]
name|columns
parameter_list|,
name|Text
name|firstRow
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|region
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|firstRow
argument_list|,
name|ts
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|region
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|column
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|region
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|versions
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|ts
parameter_list|,
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|region
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|ts
argument_list|,
name|versions
argument_list|)
return|;
block|}
block|}
comment|/**    * A class that makes a {@link Incommon} out of a {@link HTable}    */
specifier|public
specifier|static
class|class
name|HTableIncommon
implements|implements
name|Incommon
block|{
specifier|final
name|HTable
name|table
decl_stmt|;
specifier|public
name|HTableIncommon
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|void
name|abort
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|.
name|abort
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|commit
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|commit
parameter_list|(
name|long
name|lockid
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|long
name|lockid
parameter_list|,
name|Text
name|column
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|column
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|(
name|long
name|lockid
parameter_list|,
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|.
name|delete
argument_list|(
name|lockid
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deleteAll
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|.
name|deleteAll
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|startBatchUpdate
parameter_list|(
name|Text
name|row
parameter_list|)
block|{
return|return
name|this
operator|.
name|table
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
return|;
block|}
specifier|public
name|HScannerInterface
name|getScanner
parameter_list|(
name|Text
index|[]
name|columns
parameter_list|,
name|Text
name|firstRow
parameter_list|,
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|table
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
name|firstRow
argument_list|,
name|ts
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|table
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|column
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|table
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|versions
argument_list|)
return|;
block|}
specifier|public
name|byte
index|[]
index|[]
name|get
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|ts
parameter_list|,
name|int
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|table
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|ts
argument_list|,
name|versions
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

