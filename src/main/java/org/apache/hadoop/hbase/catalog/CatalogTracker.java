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
name|catalog
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ConnectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|NoRouteToHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|AtomicBoolean
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
name|HRegionInfo
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
name|NotAllMetaRegionsOnlineException
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
name|NotServingRegionException
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
name|ServerName
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
name|HConnection
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
name|HConnectionManager
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
name|RetriesExhaustedException
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
name|ipc
operator|.
name|HRegionInterface
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
name|ipc
operator|.
name|ServerNotRunningYetException
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
name|zookeeper
operator|.
name|MetaNodeTracker
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
name|zookeeper
operator|.
name|RootRegionTracker
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * Tracks the availability of the catalog tables<code>-ROOT-</code> and  *<code>.META.</code>.  *   * This class is "read-only" in that the locations of the catalog tables cannot  * be explicitly set.  Instead, ZooKeeper is used to learn of the availability  * and location of<code>-ROOT-</code>.<code>-ROOT-</code> is used to learn of  * the location of<code>.META.</code>  If not available in<code>-ROOT-</code>,  * ZooKeeper is used to monitor for a new location of<code>.META.</code>.  *  *<p>Call {@link #start()} to start up operation.  Call {@link #stop()}} to  * interrupt waits and close up shop.  */
end_comment

begin_class
specifier|public
class|class
name|CatalogTracker
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
name|CatalogTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|ZooKeeperWatcher
name|zookeeper
decl_stmt|;
specifier|private
specifier|final
name|RootRegionTracker
name|rootRegionTracker
decl_stmt|;
specifier|private
specifier|final
name|MetaNodeTracker
name|metaNodeTracker
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|metaAvailable
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|/**    * Do not clear this address once set.  Its needed when we do    * server shutdown processing -- we need to know who had .META. last.  If you    * want to know if the address is good, rely on {@link #metaAvailable} value.    */
specifier|private
name|ServerName
name|metaLocation
decl_stmt|;
specifier|private
specifier|final
name|int
name|defaultTimeout
decl_stmt|;
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ROOT_REGION
init|=
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|META_REGION
init|=
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
comment|/**    * Constructs a catalog tracker. Find current state of catalog tables and    * begin active tracking by executing {@link #start()} post construction. Does    * not timeout.    *    * @param conf    *          the {@link Configuration} from which a {@link HConnection} will be    *          obtained; if problem, this connections    *          {@link HConnection#abort(String, Throwable)} will be called.    * @throws IOException    */
specifier|public
name|CatalogTracker
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs the catalog tracker.  Find current state of catalog tables and    * begin active tracking by executing {@link #start()} post construction.    * Does not timeout.    * @param zk    * @param connection server connection    * @param abortable if fatal exception    * @throws IOException     */
specifier|public
name|CatalogTracker
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zk
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Abortable
name|abortable
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs the catalog tracker.  Find current state of catalog tables and    * begin active tracking by executing {@link #start()} post construction.    * @param zk    * @param connection server connection    * @param abortable if fatal exception    * @param defaultTimeout Timeout to use.  Pass zero for no timeout    * ({@link Object#wait(long)} when passed a<code>0</code> waits for ever).    * @throws IOException     */
specifier|public
name|CatalogTracker
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zk
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
specifier|final
name|int
name|defaultTimeout
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|zk
argument_list|,
name|conf
argument_list|,
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
argument_list|,
name|abortable
argument_list|,
name|defaultTimeout
argument_list|)
expr_stmt|;
block|}
name|CatalogTracker
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|zk
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
name|HConnection
name|connection
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
specifier|final
name|int
name|defaultTimeout
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|zookeeper
operator|=
operator|(
name|zk
operator|==
literal|null
operator|)
condition|?
name|this
operator|.
name|connection
operator|.
name|getZooKeeperWatcher
argument_list|()
else|:
name|zk
expr_stmt|;
if|if
condition|(
name|abortable
operator|==
literal|null
condition|)
block|{
name|abortable
operator|=
name|this
operator|.
name|connection
expr_stmt|;
block|}
name|this
operator|.
name|rootRegionTracker
operator|=
operator|new
name|RootRegionTracker
argument_list|(
name|zookeeper
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaNodeTracker
operator|=
operator|new
name|MetaNodeTracker
argument_list|(
name|zookeeper
argument_list|,
name|this
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultTimeout
operator|=
name|defaultTimeout
expr_stmt|;
block|}
comment|/**    * Starts the catalog tracker.    * Determines current availability of catalog tables and ensures all further    * transitions of either region are tracked.    * @throws IOException    * @throws InterruptedException     */
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|this
operator|.
name|rootRegionTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|metaNodeTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting catalog tracker "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop working.    * Interrupts any ongoing waits.    */
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|stopped
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping catalog tracker "
operator|+
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|rootRegionTracker
operator|.
name|stop
argument_list|()
expr_stmt|;
name|this
operator|.
name|metaNodeTracker
operator|.
name|stop
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|connection
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Although the {@link Closeable} interface throws an {@link
comment|// IOException}, in reality, the implementation would never do that.
name|LOG
operator|.
name|error
argument_list|(
literal|"Attempt to close catalog tracker's connection failed."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// Call this and it will interrupt any ongoing waits on meta.
synchronized|synchronized
init|(
name|this
operator|.
name|metaAvailable
init|)
block|{
name|this
operator|.
name|metaAvailable
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Gets the current location for<code>-ROOT-</code> or null if location is    * not currently available.    * @return server name    * @throws InterruptedException     */
specifier|public
name|ServerName
name|getRootLocation
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|this
operator|.
name|rootRegionTracker
operator|.
name|getRootRegionLocation
argument_list|()
return|;
block|}
comment|/**    * @return Location of server hosting meta region formatted as per    * {@link ServerName}, or null if none available    */
specifier|public
name|ServerName
name|getMetaLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaLocation
return|;
block|}
comment|/**    * Waits indefinitely for availability of<code>-ROOT-</code>.  Used during    * cluster startup.    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|void
name|waitForRoot
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|this
operator|.
name|rootRegionTracker
operator|.
name|blockUntilAvailable
argument_list|()
expr_stmt|;
block|}
comment|/**    * Gets the current location for<code>-ROOT-</code> if available and waits    * for up to the specified timeout if not immediately available.  Returns null    * if the timeout elapses before root is available.    * @param timeout maximum time to wait for root availability, in milliseconds    * @return Location of server hosting root region,    * or null if none available    * @throws InterruptedException if interrupted while waiting    * @throws NotAllMetaRegionsOnlineException if root not available before    *                                          timeout    */
name|ServerName
name|waitForRoot
parameter_list|(
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|NotAllMetaRegionsOnlineException
block|{
name|ServerName
name|sn
init|=
name|rootRegionTracker
operator|.
name|waitRootRegionLocation
argument_list|(
name|timeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NotAllMetaRegionsOnlineException
argument_list|(
literal|"Timed out; "
operator|+
name|timeout
operator|+
literal|"ms"
argument_list|)
throw|;
block|}
return|return
name|sn
return|;
block|}
comment|/**    * Gets a connection to the server hosting root, as reported by ZooKeeper,    * waiting up to the specified timeout for availability.    * @see #waitForRoot(long) for additional information    * @return connection to server hosting root    * @throws InterruptedException    * @throws NotAllMetaRegionsOnlineException if timed out waiting    * @throws IOException    */
specifier|public
name|HRegionInterface
name|waitForRootServerConnection
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
return|return
name|getCachedConnection
argument_list|(
name|waitForRoot
argument_list|(
name|timeout
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Gets a connection to the server hosting root, as reported by ZooKeeper,    * waiting for the default timeout specified on instantiation.    * @see #waitForRoot(long) for additional information    * @return connection to server hosting root    * @throws NotAllMetaRegionsOnlineException if timed out waiting    * @throws IOException    */
specifier|public
name|HRegionInterface
name|waitForRootServerConnectionDefault
parameter_list|()
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
try|try
block|{
return|return
name|getCachedConnection
argument_list|(
name|waitForRoot
argument_list|(
name|defaultTimeout
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|NotAllMetaRegionsOnlineException
argument_list|(
literal|"Interrupted"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Gets a connection to the server hosting root, as reported by ZooKeeper,    * if available.  Returns null if no location is immediately available.    * @return connection to server hosting root, null if not available    * @throws IOException    * @throws InterruptedException     */
specifier|private
name|HRegionInterface
name|getRootServerConnection
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ServerName
name|sn
init|=
name|this
operator|.
name|rootRegionTracker
operator|.
name|getRootRegionLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|sn
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|getCachedConnection
argument_list|(
name|sn
argument_list|)
return|;
block|}
comment|/**    * Gets a connection to the server currently hosting<code>.META.</code> or    * null if location is not currently available.    *<p>    * If a location is known, a connection to the cached location is returned.    * If refresh is true, the cached connection is verified first before    * returning.  If the connection is not valid, it is reset and rechecked.    *<p>    * If no location for meta is currently known, method checks ROOT for a new    * location, verifies META is currently there, and returns a cached connection    * to the server hosting META.    *    * @return connection to server hosting meta, null if location not available    * @throws IOException    * @throws InterruptedException     */
specifier|private
name|HRegionInterface
name|getMetaServerConnection
parameter_list|(
name|boolean
name|refresh
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|metaAvailable
init|)
block|{
if|if
condition|(
name|metaAvailable
operator|.
name|get
argument_list|()
condition|)
block|{
name|HRegionInterface
name|current
init|=
name|getCachedConnection
argument_list|(
name|metaLocation
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|refresh
condition|)
block|{
return|return
name|current
return|;
block|}
if|if
condition|(
name|verifyRegionLocation
argument_list|(
name|current
argument_list|,
name|this
operator|.
name|metaLocation
argument_list|,
name|META_REGION
argument_list|)
condition|)
block|{
return|return
name|current
return|;
block|}
name|resetMetaLocation
argument_list|()
expr_stmt|;
block|}
name|HRegionInterface
name|rootConnection
init|=
name|getRootServerConnection
argument_list|()
decl_stmt|;
if|if
condition|(
name|rootConnection
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ServerName
name|newLocation
init|=
name|MetaReader
operator|.
name|readMetaLocation
argument_list|(
name|rootConnection
argument_list|)
decl_stmt|;
if|if
condition|(
name|newLocation
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HRegionInterface
name|newConnection
init|=
name|getCachedConnection
argument_list|(
name|newLocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|verifyRegionLocation
argument_list|(
name|newConnection
argument_list|,
name|this
operator|.
name|metaLocation
argument_list|,
name|META_REGION
argument_list|)
condition|)
block|{
name|setMetaLocation
argument_list|(
name|newLocation
argument_list|)
expr_stmt|;
return|return
name|newConnection
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Waits indefinitely for availability of<code>.META.</code>.  Used during    * cluster startup.    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|void
name|waitForMeta
parameter_list|()
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|metaAvailable
init|)
block|{
while|while
condition|(
operator|!
name|stopped
operator|&&
operator|!
name|metaAvailable
operator|.
name|get
argument_list|()
condition|)
block|{
name|metaAvailable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Gets the current location for<code>.META.</code> if available and waits    * for up to the specified timeout if not immediately available.  Throws an    * exception if timed out waiting.  This method differs from {@link #waitForMeta()}    * in that it will go ahead and verify the location gotten from ZooKeeper by    * trying to use returned connection.    * @param timeout maximum time to wait for meta availability, in milliseconds    * @return location of meta    * @throws InterruptedException if interrupted while waiting    * @throws IOException unexpected exception connecting to meta server    * @throws NotAllMetaRegionsOnlineException if meta not available before    *                                          timeout    */
specifier|public
name|ServerName
name|waitForMeta
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
throws|,
name|NotAllMetaRegionsOnlineException
block|{
name|long
name|stop
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|timeout
decl_stmt|;
synchronized|synchronized
init|(
name|metaAvailable
init|)
block|{
while|while
condition|(
operator|!
name|stopped
operator|&&
operator|!
name|metaAvailable
operator|.
name|get
argument_list|()
operator|&&
operator|(
name|timeout
operator|==
literal|0
operator|||
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|stop
operator|)
condition|)
block|{
if|if
condition|(
name|getMetaServerConnection
argument_list|(
literal|true
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
name|metaLocation
return|;
block|}
name|metaAvailable
operator|.
name|wait
argument_list|(
name|timeout
operator|==
literal|0
condition|?
literal|50
else|:
name|timeout
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getMetaServerConnection
argument_list|(
literal|true
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NotAllMetaRegionsOnlineException
argument_list|(
literal|"Timed out ("
operator|+
name|timeout
operator|+
literal|"ms)"
argument_list|)
throw|;
block|}
return|return
name|metaLocation
return|;
block|}
block|}
comment|/**    * Gets a connection to the server hosting meta, as reported by ZooKeeper,    * waiting up to the specified timeout for availability.    * @see #waitForMeta(long) for additional information    * @return connection to server hosting meta    * @throws InterruptedException    * @throws NotAllMetaRegionsOnlineException if timed out waiting    * @throws IOException    */
specifier|public
name|HRegionInterface
name|waitForMetaServerConnection
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
return|return
name|getCachedConnection
argument_list|(
name|waitForMeta
argument_list|(
name|timeout
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Gets a connection to the server hosting meta, as reported by ZooKeeper,    * waiting up to the specified timeout for availability.    * @see #waitForMeta(long) for additional information    * @return connection to server hosting meta    * @throws NotAllMetaRegionsOnlineException if timed out or interrupted    * @throws IOException    */
specifier|public
name|HRegionInterface
name|waitForMetaServerConnectionDefault
parameter_list|()
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
try|try
block|{
return|return
name|getCachedConnection
argument_list|(
name|waitForMeta
argument_list|(
name|defaultTimeout
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|NotAllMetaRegionsOnlineException
argument_list|(
literal|"Interrupted"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|resetMetaLocation
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Current cached META location is not valid, resetting"
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaAvailable
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setMetaLocation
parameter_list|(
specifier|final
name|ServerName
name|metaLocation
parameter_list|)
block|{
name|metaAvailable
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaLocation
operator|=
name|metaLocation
expr_stmt|;
comment|// no synchronization because these are private and already under lock
name|this
operator|.
name|metaAvailable
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HRegionInterface
name|getCachedConnection
parameter_list|(
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInterface
name|protocol
init|=
literal|null
decl_stmt|;
try|try
block|{
name|protocol
operator|=
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ConnectException
condition|)
block|{
comment|// Catch this; presume it means the cached connection has gone bad.
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|SocketTimeoutException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Timed out connecting to "
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoRouteToHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connecting to "
operator|+
name|sn
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SocketException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception connecting to "
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|ioe
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|EOFException
condition|)
block|{
comment|// Catch. Other end disconnected us.
block|}
elseif|else
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|contains
argument_list|(
literal|"connection reset"
argument_list|)
condition|)
block|{
comment|// Catch. Connection reset.
block|}
else|else
block|{
throw|throw
name|ioe
throw|;
block|}
block|}
return|return
name|protocol
return|;
block|}
specifier|private
name|boolean
name|verifyRegionLocation
parameter_list|(
name|HRegionInterface
name|metaServer
parameter_list|,
specifier|final
name|ServerName
name|address
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|metaServer
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Passed metaserver is null"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|Throwable
name|t
init|=
literal|null
decl_stmt|;
try|try
block|{
return|return
name|metaServer
operator|.
name|getRegionInfo
argument_list|(
name|regionName
argument_list|)
operator|!=
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|ConnectException
name|e
parameter_list|)
block|{
name|t
operator|=
name|e
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
name|IOException
name|ioe
init|=
name|e
operator|.
name|unwrapRemoteException
argument_list|()
decl_stmt|;
name|t
operator|=
name|ioe
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|instanceof
name|EOFException
condition|)
block|{
name|t
operator|=
name|cause
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|cause
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Connection reset"
argument_list|)
condition|)
block|{
name|t
operator|=
name|cause
expr_stmt|;
block|}
else|else
block|{
name|t
operator|=
name|e
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed verification of "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
operator|+
literal|" at address="
operator|+
name|address
operator|+
literal|"; "
operator|+
name|t
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/**    * Verify<code>-ROOT-</code> is deployed and accessible.    * @param timeout How long to wait on zk for root address (passed through to    * the internal call to {@link #waitForRootServerConnection(long)}.    * @return True if the<code>-ROOT-</code> location is healthy.    * @throws IOException    * @throws InterruptedException     */
specifier|public
name|boolean
name|verifyRootRegionLocation
parameter_list|(
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|HRegionInterface
name|connection
init|=
literal|null
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|waitForRootServerConnection
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotAllMetaRegionsOnlineException
name|e
parameter_list|)
block|{
comment|// Pass
block|}
catch|catch
parameter_list|(
name|ServerNotRunningYetException
name|e
parameter_list|)
block|{
comment|// Pass -- remote server is not up so can't be carrying root
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Unexpected exception
throw|throw
name|e
throw|;
block|}
return|return
operator|(
name|connection
operator|==
literal|null
operator|)
condition|?
literal|false
else|:
name|verifyRegionLocation
argument_list|(
name|connection
argument_list|,
name|this
operator|.
name|rootRegionTracker
operator|.
name|getRootRegionLocation
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Verify<code>.META.</code> is deployed and accessible.    * @param timeout How long to wait on zk for<code>.META.</code> address    * (passed through to the internal call to {@link #waitForMetaServerConnection(long)}.    * @return True if the<code>.META.</code> location is healthy.    * @throws IOException Some unexpected IOE.    * @throws InterruptedException    */
specifier|public
name|boolean
name|verifyMetaRegionLocation
parameter_list|(
specifier|final
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
return|return
name|getMetaServerConnection
argument_list|(
literal|true
argument_list|)
operator|!=
literal|null
return|;
block|}
name|MetaNodeTracker
name|getMetaNodeTracker
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaNodeTracker
return|;
block|}
specifier|public
name|HConnection
name|getConnection
parameter_list|()
block|{
return|return
name|this
operator|.
name|connection
return|;
block|}
block|}
end_class

end_unit

