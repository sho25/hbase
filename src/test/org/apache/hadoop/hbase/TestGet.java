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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|dfs
operator|.
name|MiniDFSCluster
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Writables
import|;
end_import

begin_comment
comment|/** Test case for get */
end_comment

begin_class
specifier|public
class|class
name|TestGet
extends|extends
name|HBaseTestCase
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
name|TestGet
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|CONTENTS
init|=
operator|new
name|Text
argument_list|(
literal|"contents:"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|ROW_KEY
init|=
operator|new
name|Text
argument_list|(
name|HRegionInfo
operator|.
name|rootRegionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SERVER_ADDRESS
init|=
literal|"foo.bar.com:1234"
decl_stmt|;
specifier|private
name|void
name|verifyGet
parameter_list|(
specifier|final
name|HRegionIncommon
name|r
parameter_list|,
specifier|final
name|String
name|expectedServer
parameter_list|)
throws|throws
name|IOException
block|{
comment|// This should return a value because there is only one family member
name|byte
index|[]
name|value
init|=
name|r
operator|.
name|get
argument_list|(
name|ROW_KEY
argument_list|,
name|CONTENTS
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// This should not return a value because there are multiple family members
name|value
operator|=
name|r
operator|.
name|get
argument_list|(
name|ROW_KEY
argument_list|,
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// Find out what getFull returns
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|values
init|=
name|r
operator|.
name|getFull
argument_list|(
name|ROW_KEY
argument_list|)
decl_stmt|;
comment|// assertEquals(4, values.keySet().size());
for|for
control|(
name|Iterator
argument_list|<
name|Text
argument_list|>
name|i
init|=
name|values
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|i
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Text
name|column
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|column
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
argument_list|)
condition|)
block|{
name|String
name|server
init|=
name|Writables
operator|.
name|bytesToString
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedServer
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**     * the test    * @throws IOException    */
specifier|public
name|void
name|testGet
parameter_list|()
throws|throws
name|IOException
block|{
name|MiniDFSCluster
name|cluster
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Initialization
name|cluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|dir
init|=
operator|new
name|Path
argument_list|(
literal|"/hbase"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CONTENTS
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|desc
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
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
name|fs
operator|.
name|mkdirs
argument_list|(
name|regionDir
argument_list|)
expr_stmt|;
name|HLog
name|log
init|=
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
literal|"log"
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
operator|new
name|HRegion
argument_list|(
name|dir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegionIncommon
name|r
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Write information to the table
name|long
name|lockid
init|=
name|r
operator|.
name|startUpdate
argument_list|(
name|ROW_KEY
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|bytes
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|s
init|=
operator|new
name|DataOutputStream
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|CONTENTS
operator|.
name|write
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|CONTENTS
argument_list|,
name|bytes
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|reset
argument_list|()
expr_stmt|;
name|HRegionInfo
operator|.
name|rootRegionInfo
operator|.
name|write
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|HRegionInfo
operator|.
name|rootRegionInfo
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|lockid
operator|=
name|r
operator|.
name|startUpdate
argument_list|(
name|ROW_KEY
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|HConstants
operator|.
name|COL_SERVER
argument_list|,
name|Writables
operator|.
name|stringToBytes
argument_list|(
operator|new
name|HServerAddress
argument_list|(
name|SERVER_ADDRESS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|,
name|Writables
operator|.
name|longToBytes
argument_list|(
name|lockid
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|+
literal|"region"
argument_list|)
argument_list|,
literal|"region"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify that get works the same from memcache as when reading from disk
comment|// NOTE dumpRegion won't work here because it only reads from disk.
name|verifyGet
argument_list|(
name|r
argument_list|,
name|SERVER_ADDRESS
argument_list|)
expr_stmt|;
comment|// Close and re-open region, forcing updates to disk
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|region
operator|=
operator|new
name|HRegion
argument_list|(
name|dir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|r
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// Read it back
name|verifyGet
argument_list|(
name|r
argument_list|,
name|SERVER_ADDRESS
argument_list|)
expr_stmt|;
comment|// Update one family member and add a new one
name|lockid
operator|=
name|r
operator|.
name|startUpdate
argument_list|(
name|ROW_KEY
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|+
literal|"region"
argument_list|)
argument_list|,
literal|"region2"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|otherServerName
init|=
literal|"bar.foo.com:4321"
decl_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|HConstants
operator|.
name|COL_SERVER
argument_list|,
name|Writables
operator|.
name|stringToBytes
argument_list|(
operator|new
name|HServerAddress
argument_list|(
name|otherServerName
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
operator|+
literal|"junk"
argument_list|)
argument_list|,
literal|"junk"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|verifyGet
argument_list|(
name|r
argument_list|,
name|otherServerName
argument_list|)
expr_stmt|;
comment|// Close region and re-open it
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|region
operator|=
operator|new
name|HRegion
argument_list|(
name|dir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|r
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// Read it back
name|verifyGet
argument_list|(
name|r
argument_list|,
name|otherServerName
argument_list|)
expr_stmt|;
comment|// Close region once and for all
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|log
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|StaticTestEnvironment
operator|.
name|shutdownDfs
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

