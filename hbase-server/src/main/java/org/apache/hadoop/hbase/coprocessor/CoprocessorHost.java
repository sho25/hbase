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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|List
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|classification
operator|.
name|InterfaceStability
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
name|Abortable
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
name|DoNotRetryIOException
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
name|HBaseInterfaceAudience
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
name|TableName
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
name|HTable
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
name|HTableInterface
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
name|HTableWrapper
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
name|Bytes
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
name|CoprocessorClassLoader
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
name|SortedCopyOnWriteSet
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

begin_comment
comment|/**  * Provides the common setup framework and runtime services for coprocessor  * invocation from HBase services.  * @param<E> the specific environment extension that a concrete implementation  * provides  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|CoprocessorHost
parameter_list|<
name|E
extends|extends
name|CoprocessorEnvironment
parameter_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|REGION_COPROCESSOR_CONF_KEY
init|=
literal|"hbase.coprocessor.region.classes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_COPROCESSOR_CONF_KEY
init|=
literal|"hbase.coprocessor.regionserver.classes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USER_REGION_COPROCESSOR_CONF_KEY
init|=
literal|"hbase.coprocessor.user.region.classes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_COPROCESSOR_CONF_KEY
init|=
literal|"hbase.coprocessor.master.classes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WAL_COPROCESSOR_CONF_KEY
init|=
literal|"hbase.coprocessor.wal.classes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ABORT_ON_ERROR_KEY
init|=
literal|"hbase.coprocessor.abortonerror"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_ABORT_ON_ERROR
init|=
literal|true
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CoprocessorHost
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Abortable
name|abortable
decl_stmt|;
comment|/** Ordered set of loaded coprocessors with lock */
specifier|protected
name|SortedSet
argument_list|<
name|E
argument_list|>
name|coprocessors
init|=
operator|new
name|SortedCopyOnWriteSet
argument_list|<
name|E
argument_list|>
argument_list|(
operator|new
name|EnvironmentPriorityComparator
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
comment|// unique file prefix to use for local copies of jars when classloading
specifier|protected
name|String
name|pathPrefix
decl_stmt|;
specifier|protected
name|AtomicInteger
name|loadSequence
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|public
name|CoprocessorHost
parameter_list|(
name|Abortable
name|abortable
parameter_list|)
block|{
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
name|this
operator|.
name|pathPrefix
operator|=
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
comment|/**    * Not to be confused with the per-object _coprocessors_ (above),    * coprocessorNames is static and stores the set of all coprocessors ever    * loaded by any thread in this JVM. It is strictly additive: coprocessors are    * added to coprocessorNames, by loadInstance() but are never removed, since    * the intention is to preserve a history of all loaded coprocessors for    * diagnosis in case of server crash (HBASE-4014).    */
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|coprocessorNames
init|=
name|Collections
operator|.
name|synchronizedSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|getLoadedCoprocessors
parameter_list|()
block|{
return|return
name|coprocessorNames
return|;
block|}
comment|/**    * Used to create a parameter to the HServerLoad constructor so that    * HServerLoad can provide information about the coprocessors loaded by this    * regionserver.    * (HBASE-4070: Improve region server metrics to report loaded coprocessors    * to master).    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getCoprocessors
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|returnValue
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|CoprocessorEnvironment
name|e
range|:
name|coprocessors
control|)
block|{
name|returnValue
operator|.
name|add
argument_list|(
name|e
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|returnValue
return|;
block|}
comment|/**    * Load system coprocessors. Read the class names from configuration.    * Called by constructor.    */
specifier|protected
name|void
name|loadSystemCoprocessors
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
comment|// load default coprocessors from configure file
name|String
index|[]
name|defaultCPClasses
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
name|defaultCPClasses
operator|==
literal|null
operator|||
name|defaultCPClasses
operator|.
name|length
operator|==
literal|0
condition|)
return|return;
name|int
name|priority
init|=
name|Coprocessor
operator|.
name|PRIORITY_SYSTEM
decl_stmt|;
name|List
argument_list|<
name|E
argument_list|>
name|configured
init|=
operator|new
name|ArrayList
argument_list|<
name|E
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|className
range|:
name|defaultCPClasses
control|)
block|{
name|className
operator|=
name|className
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|findCoprocessor
argument_list|(
name|className
argument_list|)
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
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
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_SYSTEM
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"System coprocessor "
operator|+
name|className
operator|+
literal|" was loaded "
operator|+
literal|"successfully with priority ("
operator|+
name|priority
operator|++
operator|+
literal|")."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// We always abort if system coprocessors cannot be loaded
name|abortServer
argument_list|(
name|className
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
comment|// add entire set to the collection for COW efficiency
name|coprocessors
operator|.
name|addAll
argument_list|(
name|configured
argument_list|)
expr_stmt|;
block|}
comment|/**    * Load a coprocessor implementation into the host    * @param path path to implementation jar    * @param className the main class name    * @param priority chaining priority    * @param conf configuration for coprocessor    * @throws java.io.IOException Exception    */
specifier|public
name|E
name|load
parameter_list|(
name|Path
name|path
parameter_list|,
name|String
name|className
parameter_list|,
name|int
name|priority
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loading coprocessor class "
operator|+
name|className
operator|+
literal|" with path "
operator|+
name|path
operator|+
literal|" and priority "
operator|+
name|priority
argument_list|)
expr_stmt|;
name|ClassLoader
name|cl
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|implClass
operator|=
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No jar path specified for "
operator|+
name|className
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|cl
operator|=
name|CoprocessorClassLoader
operator|.
name|getClassLoader
argument_list|(
name|path
argument_list|,
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
name|pathPrefix
argument_list|,
name|conf
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
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot load external coprocessor class "
operator|+
name|className
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|//load custom code for coprocessor
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
comment|// switch temporarily to the thread classloader for custom CP
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|cl
argument_list|)
expr_stmt|;
name|E
name|cpInstance
init|=
name|loadInstance
argument_list|(
name|implClass
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|cpInstance
return|;
block|}
finally|finally
block|{
comment|// restore the fresh (host) classloader
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|hostClassLoader
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param implClass Implementation class    * @param priority priority    * @param conf configuration    * @throws java.io.IOException Exception    */
specifier|public
name|void
name|load
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|int
name|priority
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|E
name|env
init|=
name|loadInstance
argument_list|(
name|implClass
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|coprocessors
operator|.
name|add
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param implClass Implementation class    * @param priority priority    * @param conf configuration    * @throws java.io.IOException Exception    */
specifier|public
name|E
name|loadInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|int
name|priority
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|Coprocessor
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|implClass
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Configured class "
operator|+
name|implClass
operator|.
name|getName
argument_list|()
operator|+
literal|" must implement "
operator|+
name|Coprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|" interface "
argument_list|)
throw|;
block|}
comment|// create the instance
name|Coprocessor
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
name|Coprocessor
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
comment|// create the environment
name|E
name|env
init|=
name|createEnvironment
argument_list|(
name|implClass
argument_list|,
name|impl
argument_list|,
name|priority
argument_list|,
name|loadSequence
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|env
operator|instanceof
name|Environment
condition|)
block|{
operator|(
operator|(
name|Environment
operator|)
name|env
operator|)
operator|.
name|startup
argument_list|()
expr_stmt|;
block|}
comment|// HBASE-4014: maintain list of loaded coprocessors for later crash analysis
comment|// if server (master or regionserver) aborts.
name|coprocessorNames
operator|.
name|add
argument_list|(
name|implClass
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|env
return|;
block|}
comment|/**    * Called when a new Coprocessor class is loaded    */
specifier|public
specifier|abstract
name|E
name|createEnvironment
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|Coprocessor
name|instance
parameter_list|,
name|int
name|priority
parameter_list|,
name|int
name|sequence
parameter_list|,
name|Configuration
name|conf
parameter_list|)
function_decl|;
specifier|public
name|void
name|shutdown
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|Environment
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stop coprocessor "
operator|+
name|e
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
operator|(
operator|(
name|Environment
operator|)
name|e
operator|)
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Shutdown called on unknown environment: "
operator|+
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Find a coprocessor implementation by class name    * @param className the class name    * @return the coprocessor, or null if not found    */
specifier|public
name|Coprocessor
name|findCoprocessor
parameter_list|(
name|String
name|className
parameter_list|)
block|{
for|for
control|(
name|E
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|className
argument_list|)
operator|||
name|env
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|.
name|equals
argument_list|(
name|className
argument_list|)
condition|)
block|{
return|return
name|env
operator|.
name|getInstance
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Find a coprocessor environment by class name    * @param className the class name    * @return the coprocessor, or null if not found    */
specifier|public
name|CoprocessorEnvironment
name|findCoprocessorEnvironment
parameter_list|(
name|String
name|className
parameter_list|)
block|{
for|for
control|(
name|E
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|className
argument_list|)
operator|||
name|env
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|.
name|equals
argument_list|(
name|className
argument_list|)
condition|)
block|{
return|return
name|env
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Retrieves the set of classloaders used to instantiate Coprocessor classes defined in external    * jar files.    * @return A set of ClassLoader instances    */
name|Set
argument_list|<
name|ClassLoader
argument_list|>
name|getExternalClassLoaders
parameter_list|()
block|{
name|Set
argument_list|<
name|ClassLoader
argument_list|>
name|externalClassLoaders
init|=
operator|new
name|HashSet
argument_list|<
name|ClassLoader
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|ClassLoader
name|systemClassLoader
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
for|for
control|(
name|E
name|env
range|:
name|coprocessors
control|)
block|{
name|ClassLoader
name|cl
init|=
name|env
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|cl
operator|!=
name|systemClassLoader
condition|)
block|{
comment|//do not include system classloader
name|externalClassLoaders
operator|.
name|add
argument_list|(
name|cl
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|externalClassLoaders
return|;
block|}
comment|/**    * Environment priority comparator.    * Coprocessors are chained in sorted order.    */
specifier|static
class|class
name|EnvironmentPriorityComparator
implements|implements
name|Comparator
argument_list|<
name|CoprocessorEnvironment
argument_list|>
block|{
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|env1
parameter_list|,
specifier|final
name|CoprocessorEnvironment
name|env2
parameter_list|)
block|{
if|if
condition|(
name|env1
operator|.
name|getPriority
argument_list|()
operator|<
name|env2
operator|.
name|getPriority
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|env1
operator|.
name|getPriority
argument_list|()
operator|>
name|env2
operator|.
name|getPriority
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
if|if
condition|(
name|env1
operator|.
name|getLoadSequence
argument_list|()
operator|<
name|env2
operator|.
name|getLoadSequence
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|env1
operator|.
name|getLoadSequence
argument_list|()
operator|>
name|env2
operator|.
name|getLoadSequence
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
comment|/**    * Encapsulation of the environment of each coprocessor    */
specifier|public
specifier|static
class|class
name|Environment
implements|implements
name|CoprocessorEnvironment
block|{
comment|/** The coprocessor */
specifier|public
name|Coprocessor
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
comment|/** Accounting for tables opened by the coprocessor */
specifier|protected
name|List
argument_list|<
name|HTableInterface
argument_list|>
name|openTables
init|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|HTableInterface
argument_list|>
argument_list|()
argument_list|)
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
comment|/**      * Constructor      * @param impl the coprocessor instance      * @param priority chaining priority      */
specifier|public
name|Environment
parameter_list|(
specifier|final
name|Coprocessor
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
name|conf
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
specifier|protected
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
comment|// clean up any table references
for|for
control|(
name|HTableInterface
name|table
range|:
name|openTables
control|)
block|{
try|try
block|{
operator|(
operator|(
name|HTableWrapper
operator|)
name|table
operator|)
operator|.
name|internalClose
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// nothing can be done here
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to close "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Coprocessor
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
comment|/**      * Open a table from within the Coprocessor environment      * @param tableName the table name      * @return an interface for manipulating the table      * @exception java.io.IOException Exception      */
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|HTable
operator|.
name|getDefaultExecutor
argument_list|(
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Open a table from within the Coprocessor environment      * @param tableName the table name      * @return an interface for manipulating the table      * @exception java.io.IOException Exception      */
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|HTableWrapper
operator|.
name|createWrapper
argument_list|(
name|openTables
argument_list|,
name|tableName
argument_list|,
name|this
argument_list|,
name|pool
argument_list|)
return|;
block|}
block|}
specifier|protected
name|void
name|abortServer
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|environment
parameter_list|,
specifier|final
name|Throwable
name|e
parameter_list|)
block|{
name|abortServer
argument_list|(
name|environment
operator|.
name|getInstance
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|abortServer
parameter_list|(
specifier|final
name|String
name|coprocessorName
parameter_list|,
specifier|final
name|Throwable
name|e
parameter_list|)
block|{
name|String
name|message
init|=
literal|"The coprocessor "
operator|+
name|coprocessorName
operator|+
literal|" threw "
operator|+
name|e
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|abortable
operator|!=
literal|null
condition|)
block|{
name|abortable
operator|.
name|abort
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No available Abortable, process was not aborted"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This is used by coprocessor hooks which are declared to throw IOException    * (or its subtypes). For such hooks, we should handle throwable objects    * depending on the Throwable's type. Those which are instances of    * IOException should be passed on to the client. This is in conformance with    * the HBase idiom regarding IOException: that it represents a circumstance    * that should be passed along to the client for its own handling. For    * example, a coprocessor that implements access controls would throw a    * subclass of IOException, such as AccessDeniedException, in its preGet()    * method to prevent an unauthorized client's performing a Get on a particular    * table.    * @param env Coprocessor Environment    * @param e Throwable object thrown by coprocessor.    * @exception IOException Exception    */
specifier|protected
name|void
name|handleCoprocessorThrowable
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|env
parameter_list|,
specifier|final
name|Throwable
name|e
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|e
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|e
throw|;
block|}
comment|// If we got here, e is not an IOException. A loaded coprocessor has a
comment|// fatal bug, and the server (master or regionserver) should remove the
comment|// faulty coprocessor from its set of active coprocessors. Setting
comment|// 'hbase.coprocessor.abortonerror' to true will cause abortServer(),
comment|// which may be useful in development and testing environments where
comment|// 'failing fast' for error analysis is desired.
if|if
condition|(
name|env
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|ABORT_ON_ERROR_KEY
argument_list|,
name|DEFAULT_ABORT_ON_ERROR
argument_list|)
condition|)
block|{
comment|// server is configured to abort.
name|abortServer
argument_list|(
name|env
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Removing coprocessor '"
operator|+
name|env
operator|.
name|toString
argument_list|()
operator|+
literal|"' from "
operator|+
literal|"environment because it threw:  "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|coprocessors
operator|.
name|remove
argument_list|(
name|env
argument_list|)
expr_stmt|;
try|try
block|{
name|shutdown
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|x
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Uncaught exception when shutting down coprocessor '"
operator|+
name|env
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Coprocessor: '"
operator|+
name|env
operator|.
name|toString
argument_list|()
operator|+
literal|"' threw: '"
operator|+
name|e
operator|+
literal|"' and has been removed from the active "
operator|+
literal|"coprocessor set."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

