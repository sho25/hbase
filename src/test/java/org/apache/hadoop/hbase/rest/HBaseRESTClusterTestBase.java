begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HBaseClusterTestCase
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|Server
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|jetty
operator|.
name|servlet
operator|.
name|ServletHolder
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

begin_class
specifier|public
class|class
name|HBaseRESTClusterTestBase
extends|extends
name|HBaseClusterTestCase
implements|implements
name|Constants
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseRESTClusterTestBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|testServletPort
decl_stmt|;
name|Server
name|server
decl_stmt|;
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|startServletContainer
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|stopServletContainer
argument_list|()
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|startServletContainer
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ServletContainer already running"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// set up the Jersey servlet container for Jetty
name|ServletHolder
name|sh
init|=
operator|new
name|ServletHolder
argument_list|(
name|ServletContainer
operator|.
name|class
argument_list|)
decl_stmt|;
name|sh
operator|.
name|setInitParameter
argument_list|(
literal|"com.sun.jersey.config.property.resourceConfigClass"
argument_list|,
name|ResourceConfig
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|sh
operator|.
name|setInitParameter
argument_list|(
literal|"com.sun.jersey.config.property.packages"
argument_list|,
literal|"jetty"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"configured "
operator|+
name|ServletContainer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// set up Jetty and run the embedded server
name|server
operator|=
operator|new
name|Server
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|server
operator|.
name|setSendServerVersion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|server
operator|.
name|setSendDateHeader
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// set up context
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|server
argument_list|,
literal|"/"
argument_list|,
name|Context
operator|.
name|SESSIONS
argument_list|)
decl_stmt|;
name|context
operator|.
name|addServlet
argument_list|(
name|sh
argument_list|,
literal|"/*"
argument_list|)
expr_stmt|;
comment|// start the server
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// get the port
name|testServletPort
operator|=
name|server
operator|.
name|getConnectors
argument_list|()
index|[
literal|0
index|]
operator|.
name|getLocalPort
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"started "
operator|+
name|server
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" on port "
operator|+
name|testServletPort
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|stopServletContainer
parameter_list|()
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
try|try
block|{
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
name|server
operator|=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

