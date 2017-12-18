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
name|procedure
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
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|List
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
comment|/**  * Provides the common setup framework and runtime services for globally  * barriered procedure invocation from HBase services.  * @param<E> the specific procedure management extension that a concrete  * implementation provides  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|ProcedureManagerHost
parameter_list|<
name|E
extends|extends
name|ProcedureManager
parameter_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_PROCEDURE_CONF_KEY
init|=
literal|"hbase.procedure.regionserver.classes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_PROCEDURE_CONF_KEY
init|=
literal|"hbase.procedure.master.classes"
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
name|ProcedureManagerHost
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|E
argument_list|>
name|procedures
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Load system procedures. Read the class names from configuration.    * Called by constructor.    */
specifier|protected
name|void
name|loadUserProcedures
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|confKey
parameter_list|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
init|=
literal|null
decl_stmt|;
comment|// load default procedures from configure file
name|String
index|[]
name|defaultProcClasses
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|confKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|defaultProcClasses
operator|==
literal|null
operator|||
name|defaultProcClasses
operator|.
name|length
operator|==
literal|0
condition|)
return|return;
name|List
argument_list|<
name|E
argument_list|>
name|configured
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|className
range|:
name|defaultProcClasses
control|)
block|{
name|className
operator|=
name|className
operator|.
name|trim
argument_list|()
expr_stmt|;
name|ClassLoader
name|cl
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|setContextClassLoader
argument_list|(
name|cl
argument_list|)
expr_stmt|;
try|try
block|{
name|implClass
operator|=
name|cl
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|configured
operator|.
name|add
argument_list|(
name|loadInstance
argument_list|(
name|implClass
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"User procedure "
operator|+
name|className
operator|+
literal|" was loaded successfully."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Class "
operator|+
name|className
operator|+
literal|" cannot be found. "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Load procedure "
operator|+
name|className
operator|+
literal|" failed. "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// add entire set to the collection
name|procedures
operator|.
name|addAll
argument_list|(
name|configured
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|E
name|loadInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create the instance
name|E
name|impl
decl_stmt|;
name|Object
name|o
init|=
literal|null
decl_stmt|;
try|try
block|{
name|o
operator|=
name|implClass
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|impl
operator|=
operator|(
name|E
operator|)
name|o
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|impl
return|;
block|}
comment|// Register a procedure manager object
specifier|public
name|void
name|register
parameter_list|(
name|E
name|obj
parameter_list|)
block|{
name|procedures
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|E
argument_list|>
name|getProcedureManagers
parameter_list|()
block|{
name|Set
argument_list|<
name|E
argument_list|>
name|returnValue
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|E
name|e
range|:
name|procedures
control|)
block|{
name|returnValue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|returnValue
return|;
block|}
specifier|public
specifier|abstract
name|void
name|loadProcedures
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
block|}
end_class

end_unit

