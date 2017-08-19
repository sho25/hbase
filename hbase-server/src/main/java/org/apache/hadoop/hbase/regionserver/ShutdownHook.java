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
operator|.
name|regionserver
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Stoppable
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
name|ShutdownHookManager
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
name|Threads
import|;
end_import

begin_comment
comment|/**  * Manage regionserver shutdown hooks.  * @see #install(Configuration, FileSystem, Stoppable, Thread)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ShutdownHook
block|{
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
name|ShutdownHook
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLIENT_FINALIZER_DATA_METHOD
init|=
literal|"clientFinalizer"
decl_stmt|;
comment|/**    * Key for boolean configuration whose default is true.    */
specifier|public
specifier|static
specifier|final
name|String
name|RUN_SHUTDOWN_HOOK
init|=
literal|"hbase.shutdown.hook"
decl_stmt|;
comment|/**    * Key for a long configuration on how much time to wait on the fs shutdown    * hook. Default is 30 seconds.    */
specifier|public
specifier|static
specifier|final
name|String
name|FS_SHUTDOWN_HOOK_WAIT
init|=
literal|"hbase.fs.shutdown.hook.wait"
decl_stmt|;
comment|/**    * A place for keeping track of all the filesystem shutdown hooks that need    * to be executed after the last regionserver referring to a given filesystem    * stops. We keep track of the # of regionserver references in values of the map.    */
specifier|private
specifier|final
specifier|static
name|Map
argument_list|<
name|Runnable
argument_list|,
name|Integer
argument_list|>
name|fsShutdownHooks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Install a shutdown hook that calls stop on the passed Stoppable    * and then thread joins against the passed<code>threadToJoin</code>.    * When this thread completes, it then runs the hdfs thread (This install    * removes the hdfs shutdown hook keeping a handle on it to run it after    *<code>threadToJoin</code> has stopped).    *    *<p>To suppress all shutdown hook  handling -- both the running of the    * regionserver hook and of the hdfs hook code -- set    * {@link ShutdownHook#RUN_SHUTDOWN_HOOK} in {@link Configuration} to    *<code>false</code>.    * This configuration value is checked when the hook code runs.    * @param conf    * @param fs Instance of Filesystem used by the RegionServer    * @param stop Installed shutdown hook will call stop against this passed    *<code>Stoppable</code> instance.    * @param threadToJoin After calling stop on<code>stop</code> will then    * join this thread.    */
specifier|public
specifier|static
name|void
name|install
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Stoppable
name|stop
parameter_list|,
specifier|final
name|Thread
name|threadToJoin
parameter_list|)
block|{
name|Runnable
name|fsShutdownHook
init|=
name|suppressHdfsShutdownHook
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|ShutdownHookThread
argument_list|(
name|conf
argument_list|,
name|stop
argument_list|,
name|threadToJoin
argument_list|,
name|fsShutdownHook
argument_list|)
decl_stmt|;
name|ShutdownHookManager
operator|.
name|affixShutdownHook
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Installed shutdown hook thread: "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * Thread run by shutdown hook.    */
specifier|private
specifier|static
class|class
name|ShutdownHookThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|Stoppable
name|stop
decl_stmt|;
specifier|private
specifier|final
name|Thread
name|threadToJoin
decl_stmt|;
specifier|private
specifier|final
name|Runnable
name|fsShutdownHook
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
name|ShutdownHookThread
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Stoppable
name|stop
parameter_list|,
specifier|final
name|Thread
name|threadToJoin
parameter_list|,
specifier|final
name|Runnable
name|fsShutdownHook
parameter_list|)
block|{
name|super
argument_list|(
literal|"Shutdownhook:"
operator|+
name|threadToJoin
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|stop
operator|=
name|stop
expr_stmt|;
name|this
operator|.
name|threadToJoin
operator|=
name|threadToJoin
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fsShutdownHook
operator|=
name|fsShutdownHook
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|boolean
name|b
init|=
name|this
operator|.
name|conf
operator|.
name|getBoolean
argument_list|(
name|RUN_SHUTDOWN_HOOK
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutdown hook starting; "
operator|+
name|RUN_SHUTDOWN_HOOK
operator|+
literal|"="
operator|+
name|b
operator|+
literal|"; fsShutdownHook="
operator|+
name|this
operator|.
name|fsShutdownHook
argument_list|)
expr_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|this
operator|.
name|stop
operator|.
name|stop
argument_list|(
literal|"Shutdown hook"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|this
operator|.
name|threadToJoin
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|fsShutdownHook
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|fsShutdownHooks
init|)
block|{
name|int
name|refs
init|=
name|fsShutdownHooks
operator|.
name|get
argument_list|(
name|fsShutdownHook
argument_list|)
decl_stmt|;
if|if
condition|(
name|refs
operator|==
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting fs shutdown hook thread."
argument_list|)
expr_stmt|;
name|Thread
name|fsShutdownHookThread
init|=
operator|(
name|fsShutdownHook
operator|instanceof
name|Thread
operator|)
condition|?
operator|(
name|Thread
operator|)
name|fsShutdownHook
else|:
operator|new
name|Thread
argument_list|(
name|fsShutdownHook
argument_list|,
name|fsShutdownHook
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-shutdown-hook"
argument_list|)
decl_stmt|;
name|fsShutdownHookThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|fsShutdownHookThread
argument_list|,
name|this
operator|.
name|conf
operator|.
name|getLong
argument_list|(
name|FS_SHUTDOWN_HOOK_WAIT
argument_list|,
literal|30000
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|refs
operator|>
literal|0
condition|)
block|{
name|fsShutdownHooks
operator|.
name|put
argument_list|(
name|fsShutdownHook
argument_list|,
name|refs
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutdown hook finished."
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * So, HDFS keeps a static map of all FS instances. In order to make sure    * things are cleaned up on our way out, it also creates a shutdown hook    * so that all filesystems can be closed when the process is terminated; it    * calls FileSystem.closeAll. This inconveniently runs concurrently with our    * own shutdown handler, and therefore causes all the filesystems to be closed    * before the server can do all its necessary cleanup.    *    *<p>The dirty reflection in this method sneaks into the FileSystem class    * and grabs the shutdown hook, removes it from the list of active shutdown    * hooks, and returns the hook for the caller to run at its convenience.    *    *<p>This seems quite fragile and susceptible to breaking if Hadoop changes    * anything about the way this cleanup is managed. Keep an eye on things.    * @return The fs shutdown hook    * @throws RuntimeException if we fail to find or grap the shutdown hook.    */
specifier|private
specifier|static
name|Runnable
name|suppressHdfsShutdownHook
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
block|{
try|try
block|{
comment|// This introspection has been updated to work for hadoop 0.20, 0.21 and for
comment|// cloudera 0.20.  0.21 and cloudera 0.20 both have hadoop-4829.  With the
comment|// latter in place, things are a little messy in that there are now two
comment|// instances of the data member clientFinalizer; an uninstalled one in
comment|// FileSystem and one in the innner class named Cache that actually gets
comment|// registered as a shutdown hook.  If the latter is present, then we are
comment|// on 0.21 or cloudera patched 0.20.
name|Runnable
name|hdfsClientFinalizer
init|=
literal|null
decl_stmt|;
comment|// Look into the FileSystem#Cache class for clientFinalizer
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|classes
init|=
name|FileSystem
operator|.
name|class
operator|.
name|getDeclaredClasses
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|cache
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
range|:
name|classes
control|)
block|{
if|if
condition|(
name|c
operator|.
name|getSimpleName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"Cache"
argument_list|)
condition|)
block|{
name|cache
operator|=
name|c
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|cache
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"This should not happen. Could not find the cache class in FileSystem."
argument_list|)
throw|;
block|}
name|Field
name|field
init|=
literal|null
decl_stmt|;
try|try
block|{
name|field
operator|=
name|cache
operator|.
name|getDeclaredField
argument_list|(
name|CLIENT_FINALIZER_DATA_METHOD
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
comment|// We can get here if the Cache class does not have a clientFinalizer
comment|// instance: i.e. we're running on straight 0.20 w/o hadoop-4829.
block|}
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Field
name|cacheField
init|=
name|FileSystem
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"CACHE"
argument_list|)
decl_stmt|;
name|cacheField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|cacheInstance
init|=
name|cacheField
operator|.
name|get
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|hdfsClientFinalizer
operator|=
operator|(
name|Runnable
operator|)
name|field
operator|.
name|get
argument_list|(
name|cacheInstance
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Then we didnt' find clientFinalizer in Cache.  Presume clean 0.20 hadoop.
name|field
operator|=
name|FileSystem
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
name|CLIENT_FINALIZER_DATA_METHOD
argument_list|)
expr_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hdfsClientFinalizer
operator|=
operator|(
name|Runnable
operator|)
name|field
operator|.
name|get
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hdfsClientFinalizer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Client finalizer is null, can't suppress!"
argument_list|)
throw|;
block|}
synchronized|synchronized
init|(
name|fsShutdownHooks
init|)
block|{
name|boolean
name|isFSCacheDisabled
init|=
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolean
argument_list|(
literal|"fs.hdfs.impl.disable.cache"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isFSCacheDisabled
operator|&&
operator|!
name|fsShutdownHooks
operator|.
name|containsKey
argument_list|(
name|hdfsClientFinalizer
argument_list|)
operator|&&
operator|!
name|ShutdownHookManager
operator|.
name|deleteShutdownHook
argument_list|(
name|hdfsClientFinalizer
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed suppression of fs shutdown hook: "
operator|+
name|hdfsClientFinalizer
argument_list|)
throw|;
block|}
name|Integer
name|refs
init|=
name|fsShutdownHooks
operator|.
name|get
argument_list|(
name|hdfsClientFinalizer
argument_list|)
decl_stmt|;
name|fsShutdownHooks
operator|.
name|put
argument_list|(
name|hdfsClientFinalizer
argument_list|,
name|refs
operator|==
literal|null
condition|?
literal|1
else|:
name|refs
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|hdfsClientFinalizer
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|nsfe
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Couldn't find field 'clientFinalizer' in FileSystem!"
argument_list|,
name|nsfe
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to suppress HDFS shutdown hook"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|iae
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Couldn't access field 'clientFinalizer' in FileSystem!"
argument_list|,
name|iae
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to suppress HDFS shutdown hook"
argument_list|)
throw|;
block|}
block|}
comment|// Thread that does nothing. Used in below main testing.
specifier|static
class|class
name|DoNothingThread
extends|extends
name|Thread
block|{
name|DoNothingThread
parameter_list|()
block|{
name|super
argument_list|(
literal|"donothing"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|super
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Stoppable with nothing to stop.  Used below in main testing.
specifier|static
class|class
name|DoNothingStoppable
implements|implements
name|Stoppable
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
block|}
block|}
comment|/**    * Main to test basic functionality.  Run with clean hadoop 0.20 and hadoop    * 0.21 and cloudera patched hadoop to make sure our shutdown hook handling    * works for all compbinations.    * Pass '-Dhbase.shutdown.hook=false' to test turning off the running of    * shutdown hooks.    * @param args    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
name|prop
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|RUN_SHUTDOWN_HOOK
argument_list|)
decl_stmt|;
if|if
condition|(
name|prop
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|RUN_SHUTDOWN_HOOK
argument_list|,
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|prop
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Instantiate a FileSystem. This will register the fs shutdown hook.
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Thread
name|donothing
init|=
operator|new
name|DoNothingThread
argument_list|()
decl_stmt|;
name|donothing
operator|.
name|start
argument_list|()
expr_stmt|;
name|ShutdownHook
operator|.
name|install
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
operator|new
name|DoNothingStoppable
argument_list|()
argument_list|,
name|donothing
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

