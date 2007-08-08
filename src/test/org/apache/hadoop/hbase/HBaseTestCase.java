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
name|Configuration
name|conf
decl_stmt|;
specifier|protected
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
block|}
specifier|protected
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
name|long
name|regionId
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
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|regionId
argument_list|,
name|desc
argument_list|,
name|startKey
argument_list|,
name|endKey
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
name|info
operator|.
name|regionName
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
block|}
end_class

end_unit

