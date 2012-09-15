/**
 *
 */
package org.apromore.toolbox.clustering.algorithms.dbscan;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.exception.RepositoryException;
import org.apromore.service.model.ClusterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chathura Ekanayake
 */
@Service("GEDMatrix")
@Transactional(propagation = Propagation.REQUIRED)
public class InMemoryGEDMatrix {

    private static final Logger log = LoggerFactory.getLogger(InMemoryGEDMatrix.class);

    @Autowired @Qualifier("ClusteringDao")
    private ClusteringDao clusteringDao;
    @Autowired @Qualifier("FragmentVersionDao")
    private FragmentVersionDao fvDao;

    private NeighbourhoodCache neighborhoodCache;

    private Map<FragmentPair, Double> inMemoryGEDs;

    private Map<Integer, InMemoryCluster> clusters;
    private List<FragmentDataObject> noise;
    private List<FragmentDataObject> unprocessedFragments;

    private ClusterSettings settings;


    public InMemoryGEDMatrix() {
        neighborhoodCache = new NeighbourhoodCache();
    }


    public void initialize(ClusterSettings settings, Map<Integer, InMemoryCluster> clusters, List<FragmentDataObject> noise,
            List<FragmentDataObject> unprocessedFragments) throws RepositoryException {
        this.settings = settings;
        this.clusters = clusters;
        this.noise = noise;
        this.unprocessedFragments = unprocessedFragments;

        loadInMemoryGEDs();
    }

    private void loadInMemoryGEDs() throws RepositoryException {
        double maxGED = settings.getMaxNeighborGraphEditDistance();
        log.debug("Loading GEDs to memory...");
        inMemoryGEDs = clusteringDao.getDistances(maxGED);
        log.debug("GEDs have been successfully loaded to memory.");
    }


    public double getGED(Integer fid1, Integer fid2) throws RepositoryException {
        if (fid1.equals(fid2)) {
            return 0;
        }

        double gedValue = 1;
        FragmentPair pair = new FragmentPair(fvDao.findFragmentVersion(fid1), fvDao.findFragmentVersion(fid2));
        if (inMemoryGEDs.containsKey(pair)) {
            gedValue = inMemoryGEDs.get(pair);
        }

        return gedValue;
    }

    public List<FragmentDataObject> getUnsharedCoreObjectNeighborhood(FragmentDataObject o, Integer sharableClusterId,
            List<Integer> allowedIds) throws RepositoryException {
        List<FragmentDataObject> nb = getCoreObjectNeighborhood(o, allowedIds);
        if (nb == null) {
            return null;
        }

        List<FragmentDataObject> unsharedNB = new ArrayList<FragmentDataObject>();
        for (FragmentDataObject fo : nb) {

            boolean containedInSharableCluster = false;
            InMemoryCluster sharableCluster = clusters.get(sharableClusterId);
            if (sharableCluster != null) {
                containedInSharableCluster = sharableCluster.getFragments().contains(fo);
            }

            if (unprocessedFragments.contains(fo) || noise.contains(fo) || containedInSharableCluster) {
                unsharedNB.add(fo);
            }
        }

        if (!unsharedNB.contains(o)) {
            unsharedNB.add(o);
        }

        if (unsharedNB.size() < settings.getMinPoints()) {
            return null;
        } else {
            return unsharedNB;
        }
    }

    public List<FragmentDataObject> getCoreObjectNeighborhood(FragmentDataObject o, List<Integer> allowedIds) throws RepositoryException {
        List<FragmentDataObject> nb = neighborhoodCache.getNeighborhood(o.getFragment().getId());

        if (nb == null) {
            nb = getNeighbourhood(o);
            if (!nb.contains(o)) {
                nb.add(o);
            }
        }

        if (allowedIds != null) {
            List<FragmentDataObject> toBeRemoved = new ArrayList<FragmentDataObject>();
            for (FragmentDataObject nf : nb) {
                if (!allowedIds.contains(nf.getFragment().getId())) {
                    toBeRemoved.add(nf);
                }
            }
            nb.removeAll(toBeRemoved);
        }

        if (nb.size() >= settings.getMinPoints()) {
            return nb;
        } else {
            return null;
        }
    }

    /**
     * @param o
     * @return
     */
    private List<FragmentDataObject> getNeighbourhood(FragmentDataObject o) {
        FragmentVersion oid = o.getFragment();
        List<FragmentDataObject> nb = new ArrayList<FragmentDataObject>();
        Set<FragmentPair> pairs = inMemoryGEDs.keySet();
        for (FragmentPair pair : pairs) {
            if (pair.getFid1().equals(oid)) {
                nb.add(new FragmentDataObject(pair.getFid2()));
            } else if (pair.getFid2().equals(oid)) {
                nb.add(new FragmentDataObject(pair.getFid1()));
            }
        }
        if (!nb.contains(o)) {
            nb.add(o);
        }
        return nb;
    }




    public void setClusteringDao(ClusteringDao clusteringDao) {
        this.clusteringDao = clusteringDao;
    }

    /**
     * Set the Fragment Version DAO object for this class. Mainly for spring tests.
     * @param fvDAOJpa the Fragment Version Dao.
     */
    public void setFragmentVersionDao(FragmentVersionDao fvDAOJpa) {
        fvDao = fvDAOJpa;
    }

}
