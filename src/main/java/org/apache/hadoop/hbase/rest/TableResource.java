begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|Encoded
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|PathParam
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|QueryParam
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
name|client
operator|.
name|HBaseAdmin
import|;
end_import

begin_class
specifier|public
class|class
name|TableResource
extends|extends
name|ResourceBase
block|{
name|String
name|table
decl_stmt|;
comment|/**    * Constructor    * @param table    * @throws IOException    */
specifier|public
name|TableResource
parameter_list|(
name|String
name|table
parameter_list|)
throws|throws
name|IOException
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
comment|/** @return the table name */
name|String
name|getName
parameter_list|()
block|{
return|return
name|table
return|;
block|}
comment|/**    * @return true if the table exists    * @throws IOException    */
name|boolean
name|exists
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|servlet
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|admin
operator|.
name|tableExists
argument_list|(
name|table
argument_list|)
return|;
block|}
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Path
argument_list|(
literal|"exists"
argument_list|)
specifier|public
name|ExistsResource
name|getExistsResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ExistsResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"regions"
argument_list|)
specifier|public
name|RegionsResource
name|getRegionsResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|RegionsResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"scanner"
argument_list|)
specifier|public
name|ScannerResource
name|getScannerResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|ScannerResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"schema"
argument_list|)
specifier|public
name|SchemaResource
name|getSchemaResource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|SchemaResource
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"multiget"
argument_list|)
specifier|public
name|MultiRowResource
name|getMultipleRowResource
parameter_list|(
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"v"
argument_list|)
name|String
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MultiRowResource
argument_list|(
name|this
argument_list|,
name|versions
argument_list|)
return|;
block|}
annotation|@
name|Path
argument_list|(
literal|"{rowspec: .+}"
argument_list|)
specifier|public
name|RowResource
name|getRowResource
parameter_list|(
comment|// We need the @Encoded decorator so Jersey won't urldecode before
comment|// the RowSpec constructor has a chance to parse
specifier|final
annotation|@
name|PathParam
argument_list|(
literal|"rowspec"
argument_list|)
annotation|@
name|Encoded
name|String
name|rowspec
parameter_list|,
specifier|final
annotation|@
name|QueryParam
argument_list|(
literal|"v"
argument_list|)
name|String
name|versions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RowResource
argument_list|(
name|this
argument_list|,
name|rowspec
argument_list|,
name|versions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

