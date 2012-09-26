begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
operator|.
name|model
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBException
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
name|hbase
operator|.
name|SmallTests
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
name|TestStorageClusterVersionModel
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|VERSION
init|=
literal|"0.0.1-testing"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AS_XML
init|=
literal|"<ClusterVersion>"
operator|+
name|VERSION
operator|+
literal|"</ClusterVersion>"
decl_stmt|;
specifier|private
name|JAXBContext
name|context
decl_stmt|;
specifier|public
name|TestStorageClusterVersionModel
parameter_list|()
throws|throws
name|JAXBException
block|{
name|super
argument_list|()
expr_stmt|;
name|context
operator|=
name|JAXBContext
operator|.
name|newInstance
argument_list|(
name|StorageClusterVersionModel
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|StorageClusterVersionModel
name|buildTestModel
parameter_list|()
block|{
name|StorageClusterVersionModel
name|model
init|=
operator|new
name|StorageClusterVersionModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setVersion
argument_list|(
name|VERSION
argument_list|)
expr_stmt|;
return|return
name|model
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|String
name|toXML
parameter_list|(
name|StorageClusterVersionModel
name|model
parameter_list|)
throws|throws
name|JAXBException
block|{
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|context
operator|.
name|createMarshaller
argument_list|()
operator|.
name|marshal
argument_list|(
name|model
argument_list|,
name|writer
argument_list|)
expr_stmt|;
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|StorageClusterVersionModel
name|fromXML
parameter_list|(
name|String
name|xml
parameter_list|)
throws|throws
name|JAXBException
block|{
return|return
operator|(
name|StorageClusterVersionModel
operator|)
name|context
operator|.
name|createUnmarshaller
argument_list|()
operator|.
name|unmarshal
argument_list|(
operator|new
name|StringReader
argument_list|(
name|xml
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|checkModel
parameter_list|(
name|StorageClusterVersionModel
name|model
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|model
operator|.
name|getVersion
argument_list|()
argument_list|,
name|VERSION
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testBuildModel
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|buildTestModel
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFromXML
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|fromXML
argument_list|(
name|AS_XML
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

