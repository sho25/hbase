begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|codec
operator|.
name|prefixtree
operator|.
name|row
operator|.
name|data
package|;
end_package

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
name|List
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
name|KeyValue
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeTestConstants
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
name|codec
operator|.
name|prefixtree
operator|.
name|row
operator|.
name|BaseTestRowData
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
name|ByteRange
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
name|SimpleByteRange
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
name|byterange
operator|.
name|impl
operator|.
name|ByteRangeTreeSet
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

begin_comment
comment|/*  * test different timestamps  *  * http://pastebin.com/7ks8kzJ2  * http://pastebin.com/MPn03nsK  */
end_comment

begin_class
specifier|public
class|class
name|TestRowDataExerciseFInts
extends|extends
name|BaseTestRowData
block|{
specifier|static
name|List
argument_list|<
name|ByteRange
argument_list|>
name|rows
decl_stmt|;
static|static
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rowStrings
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryAa/pageAaa"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryAa/pageBbb"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryAa/pageCcc"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryAa/pageDdd"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryBb/pageEee"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryBb/pageFff"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryBb/pageGgg"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.edsBlog/directoryBb/pageHhh"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryAa/pageAaa"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryAa/pageBbb"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryAa/pageCcc"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryAa/pageDdd"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryBb/pageEee"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryBb/pageFff"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryBb/pageGgg"
argument_list|)
expr_stmt|;
name|rowStrings
operator|.
name|add
argument_list|(
literal|"com.isabellasBlog/directoryBb/pageHhh"
argument_list|)
expr_stmt|;
name|ByteRangeTreeSet
name|ba
init|=
operator|new
name|ByteRangeTreeSet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rowStrings
control|)
block|{
name|ba
operator|.
name|add
argument_list|(
operator|new
name|SimpleByteRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|rows
operator|=
name|ba
operator|.
name|compile
argument_list|()
operator|.
name|getSortedRanges
argument_list|()
expr_stmt|;
block|}
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
static|static
block|{
name|cols
operator|.
name|add
argument_list|(
literal|"Chrome"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Chromeb"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Firefox"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"InternetExplorer"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Opera"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Safari"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z1stBrowserWithHuuuuuuuuuuuugeQualifier"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z2ndBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z3rdBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z4thBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z5thBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z6thBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z7thBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z8thBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"Z9thBrowserWithEvenBiggerQualifierMoreMoreMoreMoreMore"
argument_list|)
expr_stmt|;
block|}
specifier|static
name|long
name|ts
init|=
literal|1234567890
decl_stmt|;
specifier|static
name|int
name|MAX_VALUE
init|=
literal|50
decl_stmt|;
specifier|static
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
static|static
block|{
for|for
control|(
name|ByteRange
name|row
range|:
name|rows
control|)
block|{
for|for
control|(
name|String
name|col
range|:
name|cols
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|row
operator|.
name|deepCopyToNewArray
argument_list|()
argument_list|,
name|PrefixTreeTestConstants
operator|.
name|TEST_CF
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|col
argument_list|)
argument_list|,
name|ts
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
argument_list|)
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|KeyValue
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|kvs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|individualBlockMetaAssertions
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|blockMeta
operator|.
name|getNextNodeOffsetWidth
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|blockMeta
operator|.
name|getQualifierOffsetWidth
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

