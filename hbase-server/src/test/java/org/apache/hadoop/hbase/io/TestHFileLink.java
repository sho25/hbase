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
name|io
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
name|testclassification
operator|.
name|IOTests
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
name|hbase
operator|.
name|util
operator|.
name|Pair
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
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
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Test that FileLink switches between alternate locations  * when the current location moves or gets deleted.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
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
name|TestHFileLink
block|{
annotation|@
name|Test
specifier|public
name|void
name|testValidLinkNames
parameter_list|()
block|{
name|String
name|validLinkNames
index|[]
init|=
block|{
literal|"foo=fefefe-0123456"
block|,
literal|"ns=foo=abababa-fefefefe"
block|}
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|validLinkNames
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Failed validating:"
operator|+
name|name
argument_list|,
name|name
operator|.
name|matches
argument_list|(
name|HFileLink
operator|.
name|LINK_NAME_REGEX
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|name
range|:
name|validLinkNames
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Failed validating:"
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
name|String
name|testName
init|=
literal|"foo=fefefe-0123456"
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|HFileLink
operator|.
name|getReferencedTableName
argument_list|(
name|testName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"fefefe"
argument_list|,
name|HFileLink
operator|.
name|getReferencedRegionName
argument_list|(
name|testName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"0123456"
argument_list|,
name|HFileLink
operator|.
name|getReferencedHFileName
argument_list|(
name|testName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|testName
argument_list|,
name|HFileLink
operator|.
name|createHFileLinkName
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"fefefe"
argument_list|,
literal|"0123456"
argument_list|)
argument_list|)
expr_stmt|;
name|testName
operator|=
literal|"ns=foo=fefefe-0123456"
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns"
argument_list|,
literal|"foo"
argument_list|)
argument_list|,
name|HFileLink
operator|.
name|getReferencedTableName
argument_list|(
name|testName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"fefefe"
argument_list|,
name|HFileLink
operator|.
name|getReferencedRegionName
argument_list|(
name|testName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"0123456"
argument_list|,
name|HFileLink
operator|.
name|getReferencedHFileName
argument_list|(
name|testName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|testName
argument_list|,
name|HFileLink
operator|.
name|createHFileLinkName
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns"
argument_list|,
literal|"foo"
argument_list|)
argument_list|,
literal|"fefefe"
argument_list|,
literal|"0123456"
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|name
range|:
name|validLinkNames
control|)
block|{
name|Matcher
name|m
init|=
name|HFileLink
operator|.
name|LINK_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|matches
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HFileLink
operator|.
name|getReferencedTableName
argument_list|(
name|name
argument_list|)
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HFileLink
operator|.
name|getReferencedRegionName
argument_list|(
name|name
argument_list|)
argument_list|,
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HFileLink
operator|.
name|getReferencedHFileName
argument_list|(
name|name
argument_list|)
argument_list|,
name|m
operator|.
name|group
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBackReference
parameter_list|()
block|{
name|Path
name|rootDir
init|=
operator|new
name|Path
argument_list|(
literal|"/root"
argument_list|)
decl_stmt|;
name|Path
name|archiveDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
literal|".archive"
argument_list|)
decl_stmt|;
name|String
name|storeFileName
init|=
literal|"121212"
decl_stmt|;
name|String
name|linkDir
init|=
name|FileLink
operator|.
name|BACK_REFERENCES_DIRECTORY_PREFIX
operator|+
name|storeFileName
decl_stmt|;
name|String
name|encodedRegion
init|=
literal|"FEFE"
decl_stmt|;
name|String
name|cf
init|=
literal|"cf1"
decl_stmt|;
name|TableName
name|refTables
index|[]
init|=
block|{
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"refTable"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns"
argument_list|,
literal|"refTable"
argument_list|)
block|}
decl_stmt|;
for|for
control|(
name|TableName
name|refTable
range|:
name|refTables
control|)
block|{
name|Path
name|refTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|archiveDir
argument_list|,
name|refTable
argument_list|)
decl_stmt|;
name|Path
name|refRegionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|refTableDir
argument_list|,
name|encodedRegion
argument_list|)
decl_stmt|;
name|Path
name|refDir
init|=
operator|new
name|Path
argument_list|(
name|refRegionDir
argument_list|,
name|cf
argument_list|)
decl_stmt|;
name|Path
name|refLinkDir
init|=
operator|new
name|Path
argument_list|(
name|refDir
argument_list|,
name|linkDir
argument_list|)
decl_stmt|;
name|String
name|refStoreFileName
init|=
name|refTable
operator|.
name|getNameAsString
argument_list|()
operator|.
name|replace
argument_list|(
name|TableName
operator|.
name|NAMESPACE_DELIM
argument_list|,
literal|'='
argument_list|)
operator|+
literal|"="
operator|+
name|encodedRegion
operator|+
literal|"-"
operator|+
name|storeFileName
decl_stmt|;
name|TableName
name|tableNames
index|[]
init|=
block|{
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tableName1"
argument_list|)
block|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns"
argument_list|,
literal|"tableName2"
argument_list|)
block|}
decl_stmt|;
for|for
control|(
name|TableName
name|tableName
range|:
name|tableNames
control|)
block|{
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|tableDir
argument_list|,
name|encodedRegion
argument_list|)
decl_stmt|;
name|Path
name|cfDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|cf
argument_list|)
decl_stmt|;
comment|//Verify back reference creation
name|assertEquals
argument_list|(
name|encodedRegion
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|.
name|replace
argument_list|(
name|TableName
operator|.
name|NAMESPACE_DELIM
argument_list|,
literal|'='
argument_list|)
argument_list|,
name|HFileLink
operator|.
name|createBackReferenceName
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|encodedRegion
argument_list|)
argument_list|)
expr_stmt|;
comment|//verify parsing back reference
name|Pair
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|parsedRef
init|=
name|HFileLink
operator|.
name|parseBackReferenceName
argument_list|(
name|encodedRegion
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|.
name|replace
argument_list|(
name|TableName
operator|.
name|NAMESPACE_DELIM
argument_list|,
literal|'='
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|parsedRef
operator|.
name|getFirst
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsedRef
operator|.
name|getSecond
argument_list|()
argument_list|,
name|encodedRegion
argument_list|)
expr_stmt|;
comment|//verify resolving back reference
name|Path
name|storeFileDir
init|=
operator|new
name|Path
argument_list|(
name|refLinkDir
argument_list|,
name|encodedRegion
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|.
name|replace
argument_list|(
name|TableName
operator|.
name|NAMESPACE_DELIM
argument_list|,
literal|'='
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|linkPath
init|=
operator|new
name|Path
argument_list|(
name|cfDir
argument_list|,
name|refStoreFileName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|linkPath
argument_list|,
name|HFileLink
operator|.
name|getHFileFromBackReference
argument_list|(
name|rootDir
argument_list|,
name|storeFileDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

