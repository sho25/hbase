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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|HBaseClassTestRule
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Test {@link FSUtils}.  */
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
name|TestFSVisitor
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestFSVisitor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestFSVisitor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"testtb"
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|tableFamilies
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|tableRegions
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|tableHFiles
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|tableDir
decl_stmt|;
specifier|private
name|Path
name|rootDir
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|rootDir
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"hbase"
argument_list|)
expr_stmt|;
name|tableFamilies
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|tableRegions
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|tableHFiles
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|tableDir
operator|=
name|createTableFiles
argument_list|(
name|rootDir
argument_list|,
name|TABLE_NAME
argument_list|,
name|tableRegions
argument_list|,
name|tableFamilies
argument_list|,
name|tableHFiles
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|logFileSystemState
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVisitStoreFiles
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|regions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|families
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|hfiles
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|FSVisitor
operator|.
name|visitTableStoreFiles
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|,
operator|new
name|FSVisitor
operator|.
name|StoreFileVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|String
name|hfileName
parameter_list|)
throws|throws
name|IOException
block|{
name|regions
operator|.
name|add
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|families
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|hfiles
operator|.
name|add
argument_list|(
name|hfileName
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|regions
argument_list|,
name|tableRegions
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|families
argument_list|,
name|tableFamilies
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hfiles
argument_list|,
name|tableHFiles
argument_list|)
expr_stmt|;
block|}
comment|/*    * |-testtb/    * |----f1d3ff8443297732862df21dc4e57262/    * |-------f1/    * |----------d0be84935ba84b66b1e866752ec5d663    * |----------9fc9d481718f4878b29aad0a597ecb94    * |-------f2/    * |----------4b0fe6068c564737946bcf4fd4ab8ae1    */
specifier|private
name|Path
name|createTableFiles
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|tableRegions
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|tableFamilies
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|tableHFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tableDir
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
literal|10
condition|;
operator|++
name|r
control|)
block|{
name|String
name|regionName
init|=
name|MD5Hash
operator|.
name|getMD5AsHex
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|r
argument_list|)
argument_list|)
decl_stmt|;
name|tableRegions
operator|.
name|add
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
literal|3
condition|;
operator|++
name|f
control|)
block|{
name|String
name|familyName
init|=
literal|"f"
operator|+
name|f
decl_stmt|;
name|tableFamilies
operator|.
name|add
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|familyDir
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|h
init|=
literal|0
init|;
name|h
operator|<
literal|5
condition|;
operator|++
name|h
control|)
block|{
name|String
name|hfileName
init|=
name|TEST_UTIL
operator|.
name|getRandomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"-"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|tableHFiles
operator|.
name|add
argument_list|(
name|hfileName
argument_list|)
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
name|hfileName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|tableDir
return|;
block|}
block|}
end_class

end_unit

