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
name|regionserver
package|;
end_package

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
name|HBaseTestCase
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
name|testclassification
operator|.
name|RegionServerTests
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
name|SmallTests
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
name|HFileLink
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
comment|/**  * Test HStoreFile  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestStoreFileInfo
extends|extends
name|HBaseTestCase
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Validate that we can handle valid tables with '.', '_', and '-' chars.    */
specifier|public
name|void
name|testStoreFileNames
parameter_list|()
block|{
name|String
index|[]
name|legalHFileLink
init|=
block|{
literal|"MyTable_02=abc012-def345"
block|,
literal|"MyTable_02.300=abc012-def345"
block|,
literal|"MyTable_02-400=abc012-def345"
block|,
literal|"MyTable_02-400.200=abc012-def345"
block|,
literal|"MyTable_02=abc012-def345_SeqId_1_"
block|,
literal|"MyTable_02=abc012-def345_SeqId_20_"
block|}
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|legalHFileLink
control|)
block|{
name|assertTrue
argument_list|(
literal|"should be a valid link: "
operator|+
name|name
argument_list|,
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"should be a valid StoreFile"
operator|+
name|name
argument_list|,
name|StoreFileInfo
operator|.
name|validateStoreFileName
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"should not be a valid reference: "
operator|+
name|name
argument_list|,
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|refName
init|=
name|name
operator|+
literal|".6789"
decl_stmt|;
name|assertTrue
argument_list|(
literal|"should be a valid link reference: "
operator|+
name|refName
argument_list|,
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|refName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"should be a valid StoreFile"
operator|+
name|refName
argument_list|,
name|StoreFileInfo
operator|.
name|validateStoreFileName
argument_list|(
name|refName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|illegalHFileLink
init|=
block|{
literal|".MyTable_02=abc012-def345"
block|,
literal|"-MyTable_02.300=abc012-def345"
block|,
literal|"MyTable_02-400=abc0_12-def345"
block|,
literal|"MyTable_02-400.200=abc012-def345...."
block|}
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|illegalHFileLink
control|)
block|{
name|assertFalse
argument_list|(
literal|"should not be a valid link: "
operator|+
name|name
argument_list|,
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

