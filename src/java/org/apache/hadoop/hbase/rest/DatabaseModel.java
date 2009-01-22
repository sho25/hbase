begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|HTableDescriptor
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
name|rest
operator|.
name|exception
operator|.
name|HBaseRestException
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
name|rest
operator|.
name|serializer
operator|.
name|IRestSerializer
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
name|rest
operator|.
name|serializer
operator|.
name|ISerializable
import|;
end_import

begin_import
import|import
name|agilejson
operator|.
name|TOJSON
import|;
end_import

begin_class
specifier|public
class|class
name|DatabaseModel
extends|extends
name|AbstractModel
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DatabaseModel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|DatabaseModel
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|HBaseAdmin
name|admin
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|admin
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|DatabaseMetadata
implements|implements
name|ISerializable
block|{
specifier|protected
name|boolean
name|master_running
decl_stmt|;
specifier|protected
name|HTableDescriptor
index|[]
name|tables
decl_stmt|;
specifier|public
name|DatabaseMetadata
parameter_list|(
name|HBaseAdmin
name|a
parameter_list|)
throws|throws
name|IOException
block|{
name|master_running
operator|=
name|a
operator|.
name|isMasterRunning
argument_list|()
expr_stmt|;
name|tables
operator|=
name|a
operator|.
name|listTables
argument_list|()
expr_stmt|;
block|}
annotation|@
name|TOJSON
argument_list|(
name|prefixLength
operator|=
literal|2
argument_list|)
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
block|{
return|return
name|master_running
return|;
block|}
annotation|@
name|TOJSON
specifier|public
name|HTableDescriptor
index|[]
name|getTables
parameter_list|()
block|{
return|return
name|tables
return|;
block|}
specifier|public
name|void
name|restSerialize
parameter_list|(
name|IRestSerializer
name|serializer
parameter_list|)
throws|throws
name|HBaseRestException
block|{
name|serializer
operator|.
name|serializeDatabaseMetadata
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Serialize admin ourselves to json object
comment|// rather than returning the admin object for obvious reasons
specifier|public
name|DatabaseMetadata
name|getMetadata
parameter_list|()
throws|throws
name|HBaseRestException
block|{
return|return
name|getDatabaseMetadata
argument_list|()
return|;
block|}
specifier|protected
name|DatabaseMetadata
name|getDatabaseMetadata
parameter_list|()
throws|throws
name|HBaseRestException
block|{
name|DatabaseMetadata
name|databaseMetadata
init|=
literal|null
decl_stmt|;
try|try
block|{
name|databaseMetadata
operator|=
operator|new
name|DatabaseMetadata
argument_list|(
name|this
operator|.
name|admin
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HBaseRestException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|databaseMetadata
return|;
block|}
block|}
end_class

end_unit

