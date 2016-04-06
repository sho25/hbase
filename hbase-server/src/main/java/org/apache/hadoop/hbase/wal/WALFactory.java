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
name|wal
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
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
name|List
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
name|AtomicReference
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|FSDataInputStream
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
name|hbase
operator|.
name|wal
operator|.
name|WAL
operator|.
name|Reader
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
name|wal
operator|.
name|WALProvider
operator|.
name|Writer
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
name|CancelableProgressable
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
name|EnvironmentEdgeManager
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
name|LeaseNotRecoveredException
import|;
end_import

begin_comment
comment|// imports for things that haven't moved from regionserver.wal yet.
end_comment

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
name|regionserver
operator|.
name|wal
operator|.
name|MetricsWAL
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
name|regionserver
operator|.
name|wal
operator|.
name|ProtobufLogReader
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
name|regionserver
operator|.
name|wal
operator|.
name|SequenceFileLogReader
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
name|regionserver
operator|.
name|wal
operator|.
name|WALActionsListener
import|;
end_import

begin_comment
comment|/**  * Entry point for users of the Write Ahead Log.  * Acts as the shim between internal use and the particular WALProvider we use to handle wal  * requests.  *  * Configure which provider gets used with the configuration setting "hbase.wal.provider". Available  * implementations:  *<ul>  *<li><em>defaultProvider</em> : whatever provider is standard for the hbase version. Currently  *                                  "filesystem"</li>  *<li><em>filesystem</em> : a provider that will run on top of an implementation of the Hadoop  *                             FileSystem interface, normally HDFS.</li>  *<li><em>multiwal</em> : a provider that will use multiple "filesystem" wal instances per region  *                           server.</li>  *</ul>  *  * Alternatively, you may provide a custom implementation of {@link WALProvider} by class name.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WALFactory
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
name|WALFactory
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Maps between configuration names for providers and implementation classes.    */
specifier|static
enum|enum
name|Providers
block|{
name|defaultProvider
parameter_list|(
name|DefaultWALProvider
operator|.
name|class
parameter_list|)
operator|,
constructor|filesystem(DefaultWALProvider.class
block|)
enum|,
name|multiwal
parameter_list|(
name|RegionGroupingProvider
operator|.
name|class
parameter_list|)
operator|,
constructor|asyncfs(AsyncFSWALProvider.class
block|)
class|;
end_class

begin_decl_stmt
name|Class
argument_list|<
name|?
extends|extends
name|WALProvider
argument_list|>
name|clazz
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|Providers
argument_list|(
name|Class
argument_list|<
name|?
extends|extends
name|WALProvider
argument_list|>
name|clazz
argument_list|)
block|{
name|this
operator|.
name|clazz
operator|=
name|clazz
block|;     }
end_expr_stmt

begin_decl_stmt
unit|}    public
specifier|static
specifier|final
name|String
name|WAL_PROVIDER
init|=
literal|"hbase.wal.provider"
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|String
name|DEFAULT_WAL_PROVIDER
init|=
name|Providers
operator|.
name|defaultProvider
operator|.
name|name
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|String
name|META_WAL_PROVIDER
init|=
literal|"hbase.wal.meta_provider"
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|static
specifier|final
name|String
name|DEFAULT_META_WAL_PROVIDER
init|=
name|Providers
operator|.
name|defaultProvider
operator|.
name|name
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|final
name|String
name|factoryId
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|final
name|WALProvider
name|provider
decl_stmt|;
end_decl_stmt

begin_comment
comment|// The meta updates are written to a different wal. If this
end_comment

begin_comment
comment|// regionserver holds meta regions, then this ref will be non-null.
end_comment

begin_comment
comment|// lazily intialized; most RegionServers don't deal with META
end_comment

begin_decl_stmt
specifier|final
name|AtomicReference
argument_list|<
name|WALProvider
argument_list|>
name|metaProvider
init|=
operator|new
name|AtomicReference
argument_list|<
name|WALProvider
argument_list|>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_comment
comment|/**    * Configuration-specified WAL Reader used when a custom reader is requested    */
end_comment

begin_decl_stmt
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|DefaultWALProvider
operator|.
name|Reader
argument_list|>
name|logReaderClass
decl_stmt|;
end_decl_stmt

begin_comment
comment|/**    * How long to attempt opening in-recovery wals    */
end_comment

begin_decl_stmt
specifier|private
specifier|final
name|int
name|timeoutMillis
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
end_decl_stmt

begin_comment
comment|// Used for the singleton WALFactory, see below.
end_comment

begin_constructor
specifier|private
name|WALFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// this code is duplicated here so we can keep our members final.
comment|// until we've moved reader/writer construction down into providers, this initialization must
comment|// happen prior to provider initialization, in case they need to instantiate a reader/writer.
name|timeoutMillis
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hlog.open.timeout"
argument_list|,
literal|300000
argument_list|)
expr_stmt|;
comment|/* TODO Both of these are probably specific to the fs wal provider */
name|logReaderClass
operator|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.regionserver.hlog.reader.impl"
argument_list|,
name|ProtobufLogReader
operator|.
name|class
argument_list|,
name|DefaultWALProvider
operator|.
name|Reader
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// end required early initialization
comment|// this instance can't create wals, just reader/writers.
name|provider
operator|=
literal|null
expr_stmt|;
name|factoryId
operator|=
name|SINGLETON_ID
expr_stmt|;
block|}
end_constructor

begin_function
name|Class
argument_list|<
name|?
extends|extends
name|WALProvider
argument_list|>
name|getProviderClass
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
try|try
block|{
return|return
name|Providers
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|,
name|defaultValue
argument_list|)
argument_list|)
operator|.
name|clazz
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|exception
parameter_list|)
block|{
comment|// Fall back to them specifying a class name
comment|// Note that the passed default class shouldn't actually be used, since the above only fails
comment|// when there is a config value present.
return|return
name|conf
operator|.
name|getClass
argument_list|(
name|key
argument_list|,
name|DefaultWALProvider
operator|.
name|class
argument_list|,
name|WALProvider
operator|.
name|class
argument_list|)
return|;
block|}
block|}
end_function

begin_function
name|WALProvider
name|createProvider
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|WALProvider
argument_list|>
name|clazz
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Instantiating WALProvider of type "
operator|+
name|clazz
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|WALProvider
name|result
init|=
name|clazz
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|result
operator|.
name|init
argument_list|(
name|this
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|providerId
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"couldn't set up WALProvider, the configured class is "
operator|+
name|clazz
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception details for failure to load WALProvider."
argument_list|,
name|exception
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't set up WALProvider"
argument_list|,
name|exception
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"couldn't set up WALProvider, the configured class is "
operator|+
name|clazz
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception details for failure to load WALProvider."
argument_list|,
name|exception
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't set up WALProvider"
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
end_function

begin_comment
comment|/**    * instantiate a provider from a config property.    * requires conf to have already been set (as well as anything the provider might need to read).    */
end_comment

begin_function
name|WALProvider
name|getProvider
parameter_list|(
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|String
name|defaultValue
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|WALProvider
argument_list|>
name|clazz
init|=
name|getProviderClass
argument_list|(
name|key
argument_list|,
name|defaultValue
argument_list|)
decl_stmt|;
return|return
name|createProvider
argument_list|(
name|clazz
argument_list|,
name|listeners
argument_list|,
name|providerId
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * @param conf must not be null, will keep a reference to read params in later reader/writer    *     instances.    * @param listeners may be null. will be given to all created wals (and not meta-wals)    * @param factoryId a unique identifier for this factory. used i.e. by filesystem implementations    *     to make a directory    */
end_comment

begin_constructor
specifier|public
name|WALFactory
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
name|String
name|factoryId
parameter_list|)
throws|throws
name|IOException
block|{
comment|// until we've moved reader/writer construction down into providers, this initialization must
comment|// happen prior to provider initialization, in case they need to instantiate a reader/writer.
name|timeoutMillis
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hlog.open.timeout"
argument_list|,
literal|300000
argument_list|)
expr_stmt|;
comment|/* TODO Both of these are probably specific to the fs wal provider */
name|logReaderClass
operator|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.regionserver.hlog.reader.impl"
argument_list|,
name|ProtobufLogReader
operator|.
name|class
argument_list|,
name|DefaultWALProvider
operator|.
name|Reader
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|factoryId
operator|=
name|factoryId
expr_stmt|;
comment|// end required early initialization
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.regionserver.hlog.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|provider
operator|=
name|getProvider
argument_list|(
name|WAL_PROVIDER
argument_list|,
name|DEFAULT_WAL_PROVIDER
argument_list|,
name|listeners
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// special handling of existing configuration behavior.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Running with WAL disabled."
argument_list|)
expr_stmt|;
name|provider
operator|=
operator|new
name|DisabledWALProvider
argument_list|()
expr_stmt|;
name|provider
operator|.
name|init
argument_list|(
name|this
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
name|factoryId
argument_list|)
expr_stmt|;
block|}
block|}
end_constructor

begin_comment
comment|/**    * Shutdown all WALs and clean up any underlying storage.    * Use only when you will not need to replay and edits that have gone to any wals from this    * factory.    */
end_comment

begin_function
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|WALProvider
name|metaProvider
init|=
name|this
operator|.
name|metaProvider
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|metaProvider
condition|)
block|{
name|metaProvider
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// close is called on a WALFactory with null provider in the case of contention handling
comment|// within the getInstance method.
if|if
condition|(
literal|null
operator|!=
name|provider
condition|)
block|{
name|provider
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_function

begin_comment
comment|/**    * Tell the underlying WAL providers to shut down, but do not clean up underlying storage.    * If you are not ending cleanly and will need to replay edits from this factory's wals,    * use this method if you can as it will try to leave things as tidy as possible.    */
end_comment

begin_function
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
name|IOException
name|exception
init|=
literal|null
decl_stmt|;
specifier|final
name|WALProvider
name|metaProvider
init|=
name|this
operator|.
name|metaProvider
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|metaProvider
condition|)
block|{
try|try
block|{
name|metaProvider
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|exception
operator|=
name|ioe
expr_stmt|;
block|}
block|}
name|provider
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|exception
condition|)
block|{
throw|throw
name|exception
throw|;
block|}
block|}
end_function

begin_comment
comment|/**    * @param identifier may not be null, contents will not be altered    * @param namespace could be null, and will use default namespace if null    */
end_comment

begin_function
specifier|public
name|WAL
name|getWAL
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
parameter_list|,
specifier|final
name|byte
index|[]
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|provider
operator|.
name|getWAL
argument_list|(
name|identifier
argument_list|,
name|namespace
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * @param identifier may not be null, contents will not be altered    */
end_comment

begin_function
specifier|public
name|WAL
name|getMetaWAL
parameter_list|(
specifier|final
name|byte
index|[]
name|identifier
parameter_list|)
throws|throws
name|IOException
block|{
name|WALProvider
name|metaProvider
init|=
name|this
operator|.
name|metaProvider
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|metaProvider
condition|)
block|{
specifier|final
name|WALProvider
name|temp
init|=
name|getProvider
argument_list|(
name|META_WAL_PROVIDER
argument_list|,
name|DEFAULT_META_WAL_PROVIDER
argument_list|,
name|Collections
operator|.
expr|<
name|WALActionsListener
operator|>
name|singletonList
argument_list|(
operator|new
name|MetricsWAL
argument_list|()
argument_list|)
argument_list|,
name|DefaultWALProvider
operator|.
name|META_WAL_PROVIDER_ID
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|metaProvider
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|temp
argument_list|)
condition|)
block|{
name|metaProvider
operator|=
name|temp
expr_stmt|;
block|}
else|else
block|{
comment|// reference must now be to a provider created in another thread.
name|temp
operator|.
name|close
argument_list|()
expr_stmt|;
name|metaProvider
operator|=
name|this
operator|.
name|metaProvider
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|metaProvider
operator|.
name|getWAL
argument_list|(
name|identifier
argument_list|,
literal|null
argument_list|)
return|;
block|}
end_function

begin_function
specifier|public
name|Reader
name|createReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
operator|(
name|CancelableProgressable
operator|)
literal|null
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Create a reader for the WAL. If you are reading from a file that's being written to and need    * to reopen it multiple times, use {@link WAL.Reader#reset()} instead of this method    * then just seek back to the last known good position.    * @return A WAL reader.  Close when done with it.    * @throws IOException    */
end_comment

begin_function
specifier|public
name|Reader
name|createReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|reporter
argument_list|,
literal|true
argument_list|)
return|;
block|}
end_function

begin_function
specifier|public
name|Reader
name|createReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
name|CancelableProgressable
name|reporter
parameter_list|,
name|boolean
name|allowCustom
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|DefaultWALProvider
operator|.
name|Reader
argument_list|>
name|lrClass
init|=
name|allowCustom
condition|?
name|logReaderClass
else|:
name|ProtobufLogReader
operator|.
name|class
decl_stmt|;
try|try
block|{
comment|// A wal file could be under recovery, so it may take several
comment|// tries to get it open. Instead of claiming it is corrupted, retry
comment|// to open it up to 5 minutes by default.
name|long
name|startWaiting
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|openTimeout
init|=
name|timeoutMillis
operator|+
name|startWaiting
decl_stmt|;
name|int
name|nbAttempt
init|=
literal|0
decl_stmt|;
name|FSDataInputStream
name|stream
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
if|if
condition|(
name|lrClass
operator|!=
name|ProtobufLogReader
operator|.
name|class
condition|)
block|{
comment|// User is overriding the WAL reader, let them.
name|DefaultWALProvider
operator|.
name|Reader
name|reader
init|=
name|lrClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|reader
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|reader
return|;
block|}
else|else
block|{
name|stream
operator|=
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// Note that zero-length file will fail to read PB magic, and attempt to create
comment|// a non-PB reader and fail the same way existing code expects it to. If we get
comment|// rid of the old reader entirely, we need to handle 0-size files differently from
comment|// merely non-PB files.
name|byte
index|[]
name|magic
init|=
operator|new
name|byte
index|[
name|ProtobufLogReader
operator|.
name|PB_WAL_MAGIC
operator|.
name|length
index|]
decl_stmt|;
name|boolean
name|isPbWal
init|=
operator|(
name|stream
operator|.
name|read
argument_list|(
name|magic
argument_list|)
operator|==
name|magic
operator|.
name|length
operator|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|magic
argument_list|,
name|ProtobufLogReader
operator|.
name|PB_WAL_MAGIC
argument_list|)
decl_stmt|;
name|DefaultWALProvider
operator|.
name|Reader
name|reader
init|=
name|isPbWal
condition|?
operator|new
name|ProtobufLogReader
argument_list|()
else|:
operator|new
name|SequenceFileLogReader
argument_list|()
decl_stmt|;
name|reader
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|stream
argument_list|)
expr_stmt|;
return|return
name|reader
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|stream
operator|!=
literal|null
condition|)
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not close FSDataInputStream"
operator|+
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"exception details"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
name|String
name|msg
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|msg
operator|!=
literal|null
operator|&&
operator|(
name|msg
operator|.
name|contains
argument_list|(
literal|"Cannot obtain block length"
argument_list|)
operator|||
name|msg
operator|.
name|contains
argument_list|(
literal|"Could not obtain the last block"
argument_list|)
operator|||
name|msg
operator|.
name|matches
argument_list|(
literal|"Blocklist for [^ ]* has changed.*"
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
operator|++
name|nbAttempt
operator|==
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Lease should have recovered. This is not expected. Will retry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reporter
operator|!=
literal|null
operator|&&
operator|!
name|reporter
operator|.
name|progress
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Operation is cancelled"
argument_list|)
throw|;
block|}
if|if
condition|(
name|nbAttempt
operator|>
literal|2
operator|&&
name|openTimeout
operator|<
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't open after "
operator|+
name|nbAttempt
operator|+
literal|" attempts and "
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startWaiting
operator|)
operator|+
literal|"ms "
operator|+
literal|" for "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|nbAttempt
operator|<
literal|3
condition|?
literal|500
else|:
literal|1000
argument_list|)
expr_stmt|;
continue|continue;
comment|// retry
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|InterruptedIOException
name|iioe
init|=
operator|new
name|InterruptedIOException
argument_list|()
decl_stmt|;
name|iioe
operator|.
name|initCause
argument_list|(
name|ie
argument_list|)
expr_stmt|;
throw|throw
name|iioe
throw|;
block|}
block|}
throw|throw
operator|new
name|LeaseNotRecoveredException
argument_list|(
name|e
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
name|ie
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot get log reader"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
end_function

begin_comment
comment|/**    * Create a writer for the WAL.    *<p>    * should be package-private. public only for tests and    * {@link org.apache.hadoop.hbase.regionserver.wal.Compressor}    * @return A WAL writer. Close when done with it.    * @throws IOException    */
end_comment

begin_function
specifier|public
name|Writer
name|createWALWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DefaultWALProvider
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
literal|false
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * should be package-private, visible for recovery testing.    * @return an overwritable writer for recovered edits. caller should close.    */
end_comment

begin_function
annotation|@
name|VisibleForTesting
specifier|public
name|Writer
name|createRecoveredEditsWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DefaultWALProvider
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
literal|true
argument_list|)
return|;
block|}
end_function

begin_comment
comment|// These static methods are currently used where it's impractical to
end_comment

begin_comment
comment|// untangle the reliance on state in the filesystem. They rely on singleton
end_comment

begin_comment
comment|// WALFactory that just provides Reader / Writers.
end_comment

begin_comment
comment|// For now, first Configuration object wins. Practically this just impacts the reader/writer class
end_comment

begin_decl_stmt
specifier|private
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|WALFactory
argument_list|>
name|singleton
init|=
operator|new
name|AtomicReference
argument_list|<
name|WALFactory
argument_list|>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
specifier|final
name|String
name|SINGLETON_ID
init|=
name|WALFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
end_decl_stmt

begin_comment
comment|// public only for FSHLog
end_comment

begin_function
specifier|public
specifier|static
name|WALFactory
name|getInstance
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|WALFactory
name|factory
init|=
name|singleton
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|factory
condition|)
block|{
name|WALFactory
name|temp
init|=
operator|new
name|WALFactory
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
if|if
condition|(
name|singleton
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|temp
argument_list|)
condition|)
block|{
name|factory
operator|=
name|temp
expr_stmt|;
block|}
else|else
block|{
comment|// someone else beat us to initializing
try|try
block|{
name|temp
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"failed to close temporary singleton. ignoring."
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
name|factory
operator|=
name|singleton
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|factory
return|;
block|}
end_function

begin_comment
comment|/**    * Create a reader for the given path, accept custom reader classes from conf.    * If you already have a WALFactory, you should favor the instance method.    * @return a WAL Reader, caller must close.    */
end_comment

begin_function
specifier|public
specifier|static
name|Reader
name|createReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getInstance
argument_list|(
name|configuration
argument_list|)
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Create a reader for the given path, accept custom reader classes from conf.    * If you already have a WALFactory, you should favor the instance method.    * @return a WAL Reader, caller must close.    */
end_comment

begin_function
specifier|static
name|Reader
name|createReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|,
specifier|final
name|CancelableProgressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getInstance
argument_list|(
name|configuration
argument_list|)
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|reporter
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Create a reader for the given path, ignore custom reader classes from conf.    * If you already have a WALFactory, you should favor the instance method.    * only public pending move of {@link org.apache.hadoop.hbase.regionserver.wal.Compressor}    * @return a WAL Reader, caller must close.    */
end_comment

begin_function
specifier|public
specifier|static
name|Reader
name|createReaderIgnoreCustomClass
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getInstance
argument_list|(
name|configuration
argument_list|)
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * If you already have a WALFactory, you should favor the instance method.    * @return a Writer that will overwrite files. Caller must close.    */
end_comment

begin_function
specifier|static
name|Writer
name|createRecoveredEditsWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DefaultWALProvider
operator|.
name|createWriter
argument_list|(
name|configuration
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
literal|true
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * If you already have a WALFactory, you should favor the instance method.    * @return a writer that won't overwrite files. Caller must close.    */
end_comment

begin_function
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|Writer
name|createWALWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|DefaultWALProvider
operator|.
name|createWriter
argument_list|(
name|configuration
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
literal|false
argument_list|)
return|;
block|}
end_function

begin_function
specifier|public
specifier|final
name|WALProvider
name|getWALProvider
parameter_list|()
block|{
return|return
name|this
operator|.
name|provider
return|;
block|}
end_function

begin_function
specifier|public
specifier|final
name|WALProvider
name|getMetaWALProvider
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaProvider
operator|.
name|get
argument_list|()
return|;
block|}
end_function

unit|}
end_unit

