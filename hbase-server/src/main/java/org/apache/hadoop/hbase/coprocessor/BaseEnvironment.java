begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|hbase
operator|.
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|VersionInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Encapsulation of the environment of each coprocessor  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BaseEnvironment
parameter_list|<
name|C
extends|extends
name|Coprocessor
parameter_list|>
implements|implements
name|CoprocessorEnvironment
argument_list|<
name|C
argument_list|>
block|{
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
name|BaseEnvironment
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The coprocessor */
specifier|public
name|C
name|impl
decl_stmt|;
comment|/** Chaining priority */
specifier|protected
name|int
name|priority
init|=
name|Coprocessor
operator|.
name|PRIORITY_USER
decl_stmt|;
comment|/** Current coprocessor state */
name|Coprocessor
operator|.
name|State
name|state
init|=
name|Coprocessor
operator|.
name|State
operator|.
name|UNINSTALLED
decl_stmt|;
specifier|private
name|int
name|seq
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ClassLoader
name|classLoader
decl_stmt|;
comment|/**    * Constructor    * @param impl the coprocessor instance    * @param priority chaining priority    */
specifier|public
name|BaseEnvironment
parameter_list|(
specifier|final
name|C
name|impl
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
specifier|final
name|int
name|seq
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|impl
operator|=
name|impl
expr_stmt|;
name|this
operator|.
name|classLoader
operator|=
name|impl
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|Coprocessor
operator|.
name|State
operator|.
name|INSTALLED
expr_stmt|;
name|this
operator|.
name|seq
operator|=
name|seq
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|ReadOnlyConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/** Initialize the environment */
specifier|public
name|void
name|startup
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|Coprocessor
operator|.
name|State
operator|.
name|INSTALLED
operator|||
name|state
operator|==
name|Coprocessor
operator|.
name|State
operator|.
name|STOPPED
condition|)
block|{
name|state
operator|=
name|Coprocessor
operator|.
name|State
operator|.
name|STARTING
expr_stmt|;
name|Thread
name|currentThread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|ClassLoader
name|hostClassLoader
init|=
name|currentThread
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
try|try
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|this
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|impl
operator|.
name|start
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|state
operator|=
name|Coprocessor
operator|.
name|State
operator|.
name|ACTIVE
expr_stmt|;
block|}
finally|finally
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|hostClassLoader
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not starting coprocessor "
operator|+
name|impl
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" because not inactive (state="
operator|+
name|state
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Clean up the environment */
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|state
operator|==
name|Coprocessor
operator|.
name|State
operator|.
name|ACTIVE
condition|)
block|{
name|state
operator|=
name|Coprocessor
operator|.
name|State
operator|.
name|STOPPING
expr_stmt|;
name|Thread
name|currentThread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|ClassLoader
name|hostClassLoader
init|=
name|currentThread
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
try|try
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|this
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|impl
operator|.
name|stop
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|state
operator|=
name|Coprocessor
operator|.
name|State
operator|.
name|STOPPED
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error stopping coprocessor "
operator|+
name|impl
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|hostClassLoader
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not stopping coprocessor "
operator|+
name|impl
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" because not active (state="
operator|+
name|state
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|C
name|getInstance
parameter_list|()
block|{
return|return
name|impl
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClassLoader
name|getClassLoader
parameter_list|()
block|{
return|return
name|classLoader
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getLoadSequence
parameter_list|()
block|{
return|return
name|seq
return|;
block|}
comment|/** @return the coprocessor environment version */
annotation|@
name|Override
specifier|public
name|int
name|getVersion
parameter_list|()
block|{
return|return
name|Coprocessor
operator|.
name|VERSION
return|;
block|}
comment|/** @return the HBase release */
annotation|@
name|Override
specifier|public
name|String
name|getHBaseVersion
parameter_list|()
block|{
return|return
name|VersionInfo
operator|.
name|getVersion
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

