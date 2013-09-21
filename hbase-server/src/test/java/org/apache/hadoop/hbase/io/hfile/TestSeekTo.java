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
name|io
operator|.
name|hfile
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FSDataOutputStream
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
name|io
operator|.
name|RawComparator
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
comment|/**  * Test {@link HFileScanner#seekTo(byte[])} and its variants.  */
end_comment

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
name|TestSeekTo
extends|extends
name|HBaseTestCase
block|{
specifier|static
name|boolean
name|switchKVs
init|=
literal|false
decl_stmt|;
specifier|static
name|KeyValue
name|toKV
parameter_list|(
name|String
name|row
parameter_list|,
name|TagUsage
name|tagUsage
parameter_list|)
block|{
if|if
condition|(
name|tagUsage
operator|==
name|TagUsage
operator|.
name|NO_TAG
condition|)
block|{
return|return
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|tagUsage
operator|==
name|TagUsage
operator|.
name|ONLY_TAG
condition|)
block|{
name|Tag
name|t
init|=
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"myTag1"
argument_list|)
decl_stmt|;
name|Tag
index|[]
name|tags
init|=
operator|new
name|Tag
index|[
literal|1
index|]
decl_stmt|;
name|tags
index|[
literal|0
index|]
operator|=
name|t
expr_stmt|;
return|return
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|tags
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|switchKVs
condition|)
block|{
name|switchKVs
operator|=
literal|true
expr_stmt|;
return|return
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
name|switchKVs
operator|=
literal|false
expr_stmt|;
name|Tag
name|t
init|=
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"myTag1"
argument_list|)
decl_stmt|;
name|Tag
index|[]
name|tags
init|=
operator|new
name|Tag
index|[
literal|1
index|]
decl_stmt|;
name|tags
index|[
literal|0
index|]
operator|=
name|t
expr_stmt|;
return|return
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|tags
argument_list|)
return|;
block|}
block|}
block|}
specifier|static
name|String
name|toRowStr
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
name|Path
name|makeNewFile
parameter_list|(
name|TagUsage
name|tagUsage
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|ncTFile
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|testDir
argument_list|,
literal|"basic.hfile"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tagUsage
operator|!=
name|TagUsage
operator|.
name|NO_TAG
condition|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
name|FSDataOutputStream
name|fout
init|=
name|this
operator|.
name|fs
operator|.
name|create
argument_list|(
name|ncTFile
argument_list|)
decl_stmt|;
name|int
name|blocksize
init|=
name|toKV
argument_list|(
literal|"a"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getLength
argument_list|()
operator|*
literal|3
decl_stmt|;
name|HFileContext
name|context
init|=
operator|new
name|HFileContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|setBlocksize
argument_list|(
name|blocksize
argument_list|)
expr_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactoryNoCache
argument_list|(
name|conf
argument_list|)
operator|.
name|withOutputStream
argument_list|(
name|fout
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
comment|// NOTE: This test is dependent on this deprecated nonstandard
comment|// comparator
operator|.
name|withComparator
argument_list|(
name|KeyValue
operator|.
name|RAW_COMPARATOR
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// 4 bytes * 3 * 2 for each key/value +
comment|// 3 for keys, 15 for values = 42 (woot)
name|writer
operator|.
name|append
argument_list|(
name|toKV
argument_list|(
literal|"c"
argument_list|,
name|tagUsage
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
argument_list|)
expr_stmt|;
comment|// block transition
name|writer
operator|.
name|append
argument_list|(
name|toKV
argument_list|(
literal|"i"
argument_list|,
name|tagUsage
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|toKV
argument_list|(
literal|"k"
argument_list|,
name|tagUsage
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|ncTFile
return|;
block|}
specifier|public
name|void
name|testSeekBefore
parameter_list|()
throws|throws
name|Exception
block|{
name|testSeekBeforeInternals
argument_list|(
name|TagUsage
operator|.
name|NO_TAG
argument_list|)
expr_stmt|;
name|testSeekBeforeInternals
argument_list|(
name|TagUsage
operator|.
name|ONLY_TAG
argument_list|)
expr_stmt|;
name|testSeekBeforeInternals
argument_list|(
name|TagUsage
operator|.
name|PARTIAL_TAG
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testSeekBeforeInternals
parameter_list|(
name|TagUsage
name|tagUsage
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|makeNewFile
argument_list|(
name|tagUsage
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"a"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"c"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"d"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"f"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"h"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"i"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"j"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"k"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"l"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"k"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testSeekBeforeWithReSeekTo
parameter_list|()
throws|throws
name|Exception
block|{
name|testSeekBeforeWithReSeekToInternals
argument_list|(
name|TagUsage
operator|.
name|NO_TAG
argument_list|)
expr_stmt|;
name|testSeekBeforeWithReSeekToInternals
argument_list|(
name|TagUsage
operator|.
name|ONLY_TAG
argument_list|)
expr_stmt|;
name|testSeekBeforeWithReSeekToInternals
argument_list|(
name|TagUsage
operator|.
name|PARTIAL_TAG
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testSeekBeforeWithReSeekToInternals
parameter_list|(
name|TagUsage
name|tagUsage
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|makeNewFile
argument_list|(
name|tagUsage
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"a"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"b"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"c"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore d, so the scanner points to c
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"d"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo e and g
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"c"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore e, so the scanner points to c
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo e and g
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore f, so the scanner points to e
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"f"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo e and g
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore g, so the scanner points to e
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo e and g again
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"e"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore h, so the scanner points to g
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"h"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo g
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore i, so the scanner points to g
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"i"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo g
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore j, so the scanner points to i
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"j"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo i
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"i"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore k, so the scanner points to i
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"k"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo i and k
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"i"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"k"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"k"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// seekBefore l, so the scanner points to k
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|scanner
operator|.
name|seekBefore
argument_list|(
name|toKV
argument_list|(
literal|"l"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"k"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// reseekTo k
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|scanner
operator|.
name|reseekTo
argument_list|(
name|toKV
argument_list|(
literal|"k"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"k"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSeekTo
parameter_list|()
throws|throws
name|Exception
block|{
name|testSeekToInternals
argument_list|(
name|TagUsage
operator|.
name|NO_TAG
argument_list|)
expr_stmt|;
name|testSeekToInternals
argument_list|(
name|TagUsage
operator|.
name|ONLY_TAG
argument_list|)
expr_stmt|;
name|testSeekToInternals
argument_list|(
name|TagUsage
operator|.
name|PARTIAL_TAG
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testSeekToInternals
parameter_list|(
name|TagUsage
name|tagUsage
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|makeNewFile
argument_list|(
name|tagUsage
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|reader
operator|.
name|getDataBlockIndexReader
argument_list|()
operator|.
name|getRootBlockCount
argument_list|()
argument_list|)
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// lies before the start of the file.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|(
name|toKV
argument_list|(
literal|"a"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|(
name|toKV
argument_list|(
literal|"d"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Across a block boundary now.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|(
name|toKV
argument_list|(
literal|"h"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|(
name|toKV
argument_list|(
literal|"l"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"k"
argument_list|,
name|toRowStr
argument_list|(
name|scanner
operator|.
name|getKeyValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testBlockContainingKey
parameter_list|()
throws|throws
name|Exception
block|{
name|testBlockContainingKeyInternals
argument_list|(
name|TagUsage
operator|.
name|NO_TAG
argument_list|)
expr_stmt|;
name|testBlockContainingKeyInternals
argument_list|(
name|TagUsage
operator|.
name|ONLY_TAG
argument_list|)
expr_stmt|;
name|testBlockContainingKeyInternals
argument_list|(
name|TagUsage
operator|.
name|PARTIAL_TAG
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testBlockContainingKeyInternals
parameter_list|(
name|TagUsage
name|tagUsage
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|makeNewFile
argument_list|(
name|tagUsage
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|HFileBlockIndex
operator|.
name|BlockIndexReader
name|blockIndexReader
init|=
name|reader
operator|.
name|getDataBlockIndexReader
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|blockIndexReader
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|klen
init|=
name|toKV
argument_list|(
literal|"a"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|length
decl_stmt|;
comment|// falls before the start of the file.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"a"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"c"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"d"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"e"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"g"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"h"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"i"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"j"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"k"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|blockIndexReader
operator|.
name|rootBlockContainingKey
argument_list|(
name|toKV
argument_list|(
literal|"l"
argument_list|,
name|tagUsage
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|,
name|klen
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

