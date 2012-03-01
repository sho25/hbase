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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletContext
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
name|annotation
operator|.
name|XmlAttribute
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
name|annotation
operator|.
name|XmlRootElement
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
name|classification
operator|.
name|InterfaceAudience
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
name|ProtobufMessageHandler
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
name|RESTServlet
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
name|protobuf
operator|.
name|generated
operator|.
name|VersionMessage
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jersey
operator|.
name|spi
operator|.
name|container
operator|.
name|servlet
operator|.
name|ServletContainer
import|;
end_import

begin_comment
comment|/**  * A representation of the collection of versions of the REST gateway software  * components.  *<ul>  *<li>restVersion: REST gateway revision</li>  *<li>jvmVersion: the JVM vendor and version information</li>  *<li>osVersion: the OS type, version, and hardware architecture</li>  *<li>serverVersion: the name and version of the servlet container</li>  *<li>jerseyVersion: the version of the embedded Jersey framework</li>  *</ul>  */
end_comment

begin_class
annotation|@
name|XmlRootElement
argument_list|(
name|name
operator|=
literal|"Version"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VersionModel
implements|implements
name|Serializable
implements|,
name|ProtobufMessageHandler
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|restVersion
decl_stmt|;
specifier|private
name|String
name|jvmVersion
decl_stmt|;
specifier|private
name|String
name|osVersion
decl_stmt|;
specifier|private
name|String
name|serverVersion
decl_stmt|;
specifier|private
name|String
name|jerseyVersion
decl_stmt|;
comment|/**    * Default constructor. Do not use.    */
specifier|public
name|VersionModel
parameter_list|()
block|{}
comment|/**    * Constructor    * @param context the servlet context    */
specifier|public
name|VersionModel
parameter_list|(
name|ServletContext
name|context
parameter_list|)
block|{
name|restVersion
operator|=
name|RESTServlet
operator|.
name|VERSION_STRING
expr_stmt|;
name|jvmVersion
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.vm.vendor"
argument_list|)
operator|+
literal|' '
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.version"
argument_list|)
operator|+
literal|'-'
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.vm.version"
argument_list|)
expr_stmt|;
name|osVersion
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.name"
argument_list|)
operator|+
literal|' '
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.version"
argument_list|)
operator|+
literal|' '
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.arch"
argument_list|)
expr_stmt|;
name|serverVersion
operator|=
name|context
operator|.
name|getServerInfo
argument_list|()
expr_stmt|;
name|jerseyVersion
operator|=
name|ServletContainer
operator|.
name|class
operator|.
name|getPackage
argument_list|()
operator|.
name|getImplementationVersion
argument_list|()
expr_stmt|;
block|}
comment|/** 	 * @return the REST gateway version 	 */
annotation|@
name|XmlAttribute
argument_list|(
name|name
operator|=
literal|"REST"
argument_list|)
specifier|public
name|String
name|getRESTVersion
parameter_list|()
block|{
return|return
name|restVersion
return|;
block|}
comment|/** 	 * @return the JVM vendor and version 	 */
annotation|@
name|XmlAttribute
argument_list|(
name|name
operator|=
literal|"JVM"
argument_list|)
specifier|public
name|String
name|getJVMVersion
parameter_list|()
block|{
return|return
name|jvmVersion
return|;
block|}
comment|/**    * @return the OS name, version, and hardware architecture    */
annotation|@
name|XmlAttribute
argument_list|(
name|name
operator|=
literal|"OS"
argument_list|)
specifier|public
name|String
name|getOSVersion
parameter_list|()
block|{
return|return
name|osVersion
return|;
block|}
comment|/**    * @return the servlet container version    */
annotation|@
name|XmlAttribute
argument_list|(
name|name
operator|=
literal|"Server"
argument_list|)
specifier|public
name|String
name|getServerVersion
parameter_list|()
block|{
return|return
name|serverVersion
return|;
block|}
comment|/**    * @return the version of the embedded Jersey framework    */
annotation|@
name|XmlAttribute
argument_list|(
name|name
operator|=
literal|"Jersey"
argument_list|)
specifier|public
name|String
name|getJerseyVersion
parameter_list|()
block|{
return|return
name|jerseyVersion
return|;
block|}
comment|/**    * @param version the REST gateway version string    */
specifier|public
name|void
name|setRESTVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
name|this
operator|.
name|restVersion
operator|=
name|version
expr_stmt|;
block|}
comment|/**    * @param version the OS version string    */
specifier|public
name|void
name|setOSVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
name|this
operator|.
name|osVersion
operator|=
name|version
expr_stmt|;
block|}
comment|/**    * @param version the JVM version string    */
specifier|public
name|void
name|setJVMVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
name|this
operator|.
name|jvmVersion
operator|=
name|version
expr_stmt|;
block|}
comment|/**    * @param version the servlet container version string    */
specifier|public
name|void
name|setServerVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
name|this
operator|.
name|serverVersion
operator|=
name|version
expr_stmt|;
block|}
comment|/**    * @param version the Jersey framework version string    */
specifier|public
name|void
name|setJerseyVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
name|this
operator|.
name|jerseyVersion
operator|=
name|version
expr_stmt|;
block|}
comment|/* (non-Javadoc) 	 * @see java.lang.Object#toString() 	 */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"rest "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|restVersion
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" [JVM: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|jvmVersion
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"] [OS: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|osVersion
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"] [Server: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|serverVersion
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"] [Jersey: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|jerseyVersion
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|createProtobufOutput
parameter_list|()
block|{
name|Version
operator|.
name|Builder
name|builder
init|=
name|Version
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setRestVersion
argument_list|(
name|restVersion
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setJvmVersion
argument_list|(
name|jvmVersion
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setOsVersion
argument_list|(
name|osVersion
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setServerVersion
argument_list|(
name|serverVersion
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setJerseyVersion
argument_list|(
name|jerseyVersion
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProtobufMessageHandler
name|getObjectFromMessage
parameter_list|(
name|byte
index|[]
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|Version
operator|.
name|Builder
name|builder
init|=
name|Version
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|mergeFrom
argument_list|(
name|message
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|hasRestVersion
argument_list|()
condition|)
block|{
name|restVersion
operator|=
name|builder
operator|.
name|getRestVersion
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|hasJvmVersion
argument_list|()
condition|)
block|{
name|jvmVersion
operator|=
name|builder
operator|.
name|getJvmVersion
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|hasOsVersion
argument_list|()
condition|)
block|{
name|osVersion
operator|=
name|builder
operator|.
name|getOsVersion
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|hasServerVersion
argument_list|()
condition|)
block|{
name|serverVersion
operator|=
name|builder
operator|.
name|getServerVersion
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|builder
operator|.
name|hasJerseyVersion
argument_list|()
condition|)
block|{
name|jerseyVersion
operator|=
name|builder
operator|.
name|getJerseyVersion
argument_list|()
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

