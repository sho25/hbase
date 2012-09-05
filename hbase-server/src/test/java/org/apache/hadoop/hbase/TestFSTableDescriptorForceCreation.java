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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|util
operator|.
name|FSTableDescriptors
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

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
name|TestFSTableDescriptorForceCreation
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
name|testShouldCreateNewTableDescriptorIfForcefulCreationIsFalse
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"newTable2"
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
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|name
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
name|assertTrue
argument_list|(
literal|"Should create new table descriptor"
argument_list|,
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShouldNotCreateTheSameTableDescriptorIfForcefulCreationIsFalse
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
literal|"testAlreadyExists"
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
name|UTIL
operator|.
name|getDataTestDir
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
name|assertFalse
argument_list|(
literal|"Should not create new table descriptor"
argument_list|,
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShouldAllowForcefulCreationOfAlreadyExistingTableDescriptor
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|name
init|=
literal|"createNewTableNew2"
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
name|Path
name|rootdir
init|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|name
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
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Should create new table descriptor"
argument_list|,
name|FSTableDescriptors
operator|.
name|createTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|htd
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

